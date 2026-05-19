package com.example.deustobank.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.AccountResponse;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.service.AlertService;
import com.example.deustobank.service.AuthService;
import com.example.deustobank.service.ExportService;
import com.example.deustobank.service.NotificationService;
import com.example.deustobank.service.PdfService;
import com.example.deustobank.service.UserService;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Direct dependencies of AccountController ──────────────────────────────
    @MockBean private AccountService service;
    @MockBean private TransactionRepository transactionRepo;
    @MockBean private PdfService pdfService;

    // ── Extra beans required by the Spring context in WebMvcTest ─────────────
    @MockBean private AlertService alertService;
    @MockBean private AuthService authService;
    @MockBean private NotificationService notificationService;
    @MockBean private ExportService exportService;
    @MockBean private UserService userService;

    private User owner;
    private Account testAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setFullName("Test Owner");
        owner.setEmail("owner@test.com");
        owner.setRole("USER");
        owner.setActive(true);

        testAccount = new Account();
        testAccount.setId(10L);
        testAccount.setOwnerName("Test Owner");
        testAccount.setBalance(1000.0);
        testAccount.setUser(owner);

        testTransaction = new Transaction();
        testTransaction.setId(100L);
        testTransaction.setType("DEPOSIT");
        testTransaction.setAmount(200.0);
        testTransaction.setDate(LocalDateTime.now());
        testTransaction.setBalanceBefore(800.0);
        testTransaction.setBalanceAfter(1000.0);
        testTransaction.setAccount(testAccount);
    }

    // ── GET /accounts ─────────────────────────────────────────────────────────

    @Test
    void getAll_Success() throws Exception {
        when(service.getAll()).thenReturn(List.of(testAccount));

        mockMvc.perform(get("/accounts"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(10));
    }

    // ── GET /accounts/user/{userId} ───────────────────────────────────────────

    @Test
    void getByUser_Success() throws Exception {
        when(service.getAccountsByUser(1L)).thenReturn(List.of(testAccount));

        mockMvc.perform(get("/accounts/user/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].ownerName").value("Test Owner"));
    }

    // ── GET /accounts/{id} ────────────────────────────────────────────────────

    @Test
    void getById_Success() throws Exception {
        when(service.getById(10L)).thenReturn(testAccount);

        mockMvc.perform(get("/accounts/10"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(10))
               .andExpect(jsonPath("$.balance").value(1000.0));
    }

    // ── GET /accounts/{id}/transactions ───────────────────────────────────────

    @Test
    void getTransactions_NoDateFilter_Success() throws Exception {
        when(service.getById(10L)).thenReturn(testAccount);
        when(transactionRepo.findByAccountIdOrderByDateDesc(10L))
                .thenReturn(List.of(testTransaction));

        mockMvc.perform(get("/accounts/10/transactions")
                        .param("requesterId", "1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void getTransactions_WithDateFilter_Success() throws Exception {
        when(service.getById(10L)).thenReturn(testAccount);
        when(transactionRepo.findByAccountIdAndDateBetween(
                eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testTransaction));

        mockMvc.perform(get("/accounts/10/transactions")
                        .param("requesterId", "1")
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

    @Test
    void getTransactions_Unauthorized_Returns403() throws Exception {
        when(service.getById(10L)).thenReturn(testAccount);

        // requesterId=99 does not match account owner id=1
        mockMvc.perform(get("/accounts/10/transactions")
                        .param("requesterId", "99"))
               .andExpect(status().isForbidden())
               .andExpect(content().string("No autorizado"));
    }

    // ── GET /accounts/transactions/{id} ───────────────────────────────────────

    @Test
    void getTransactionById_Found() throws Exception {
        when(transactionRepo.findByIdWithDetails(100L))
                .thenReturn(Optional.of(testTransaction));

        mockMvc.perform(get("/accounts/transactions/100"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void getTransactionById_NotFound_Returns404() throws Exception {
        when(transactionRepo.findByIdWithDetails(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/accounts/transactions/999"))
               .andExpect(status().isNotFound());
    }

    // ── POST /accounts ────────────────────────────────────────────────────────

    @Test
    void create_Success() throws Exception {
        when(service.create(any(Account.class), eq(1L))).thenReturn(testAccount);

        String body = "{\"ownerName\":\"Test Owner\",\"balance\":1000.0}";

        mockMvc.perform(post("/accounts")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(10));
    }

    // ── PUT /accounts/{id}/deposit ────────────────────────────────────────────

    @Test
    void deposit_Success() throws Exception {
        // concepto is @RequestParam(required=false) — use any() to accept both null and string
        when(service.deposit(eq(10L), eq(500.0), eq(1L), any()))
                .thenReturn(testAccount);

        mockMvc.perform(put("/accounts/10/deposit")
                        .param("amount", "500.0")
                        .param("requesterId", "1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deposit_ServiceThrows_ReturnsBadRequest() throws Exception {
        when(service.deposit(eq(10L), eq(500.0), eq(1L), any()))
                .thenThrow(new RuntimeException("Error interno"));

        // Controller catches RuntimeException and returns 400 with fixed string "Error deposit"
        mockMvc.perform(put("/accounts/10/deposit")
                        .param("amount", "500.0")
                        .param("requesterId", "1"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Error deposit"));
    }

    // ── PUT /accounts/{id}/withdraw ───────────────────────────────────────────

    @Test
    void withdraw_Success() throws Exception {
        AccountResponse response = new AccountResponse(testAccount, null);
        when(service.withdraw(eq(10L), eq(200.0), eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/accounts/10/withdraw")
                        .param("amount", "200.0")
                        .param("requesterId", "1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.account.id").value(10));
    }

    @Test
    void withdraw_ServiceThrows_ReturnsBadRequestWithMessage() throws Exception {
        when(service.withdraw(eq(10L), eq(9999.0), eq(1L), any()))
                .thenThrow(new RuntimeException("Saldo insuficiente"));

        // Controller catches RuntimeException and returns 400 with e.getMessage()
        mockMvc.perform(put("/accounts/10/withdraw")
                        .param("amount", "9999.0")
                        .param("requesterId", "1"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Saldo insuficiente"));
    }

    // ── POST /accounts/transfer ───────────────────────────────────────────────

    @Test
    void transfer_Success_NoAlert() throws Exception {
        when(service.transfer(10L, 20L, 100.0, 1L)).thenReturn(null);

        mockMvc.perform(post("/accounts/transfer")
                        .param("fromId", "10")
                        .param("toId", "20")
                        .param("amount", "100.0")
                        .param("requesterId", "1"))
               .andExpect(status().isOk())
               .andExpect(content().string("Transferencia realizada"));
    }

    @Test
    void transfer_Success_WithLowBalanceAlert() throws Exception {
        when(service.transfer(10L, 20L, 100.0, 1L))
                .thenReturn("Saldo bajo detectado");

        mockMvc.perform(post("/accounts/transfer")
                        .param("fromId", "10")
                        .param("toId", "20")
                        .param("amount", "100.0")
                        .param("requesterId", "1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Transferencia realizada"))
               .andExpect(jsonPath("$.alert").value("Saldo bajo detectado"));
    }

    @Test
    void transfer_ServiceThrows_ReturnsBadRequestWithMessage() throws Exception {
        when(service.transfer(10L, 20L, 100.0, 1L))
                .thenThrow(new RuntimeException("Cuenta no encontrada"));

        mockMvc.perform(post("/accounts/transfer")
                        .param("fromId", "10")
                        .param("toId", "20")
                        .param("amount", "100.0")
                        .param("requesterId", "1"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Cuenta no encontrada"));
    }

    // ── DELETE /accounts/{id} ─────────────────────────────────────────────────

    @Test
    void delete_Success() throws Exception {
        doNothing().when(service).deleteAccount(10L, 1L);

        mockMvc.perform(delete("/accounts/10")
                        .param("requesterId", "1"))
               .andExpect(status().isOk())
               .andExpect(content().string("Cuenta eliminada correctamente"));
    }

    // ── PUT /accounts/{id}/limite ─────────────────────────────────────────────

    @Test
    void setLimiteGastoMensual_Success() throws Exception {
        testAccount.setLimiteGastoMensual(500.0);
        when(service.setLimiteGastoMensual(10L, 500.0)).thenReturn(testAccount);

        mockMvc.perform(put("/accounts/10/limite")
                        .param("limite", "500.0"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.limiteGastoMensual").value(500.0));
    }

    // ── PUT /accounts/{id}/umbral ─────────────────────────────────────────────

    @Test
    void setUmbralSaldoBajo_Success() throws Exception {
        testAccount.setUmbralSaldoBajo(100.0);
        when(service.setUmbralSaldoBajo(10L, 100.0)).thenReturn(testAccount);

        mockMvc.perform(put("/accounts/10/umbral")
                        .param("umbral", "100.0"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.umbralSaldoBajo").value(100.0));
    }

    // ── GET /accounts/{id}/statement/pdf ─────────────────────────────────────

    @Test
    void getPdfStatement_Success() throws Exception {
        when(service.getById(10L)).thenReturn(testAccount);
        when(transactionRepo.findByAccountIdOrderByDateDesc(10L))
                .thenReturn(List.of(testTransaction));
        when(pdfService.generateStatement(any(Account.class), any()))
                .thenReturn("pdf-content".getBytes());

        mockMvc.perform(get("/accounts/10/statement/pdf")
                        .param("requesterId", "1"))
               .andExpect(status().isOk())
               .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void getPdfStatement_Unauthorized_Returns403() throws Exception {
        when(service.getById(10L)).thenReturn(testAccount);

        // requesterId=99 does not match account owner id=1
        mockMvc.perform(get("/accounts/10/statement/pdf")
                        .param("requesterId", "99"))
               .andExpect(status().isForbidden());
    }
}