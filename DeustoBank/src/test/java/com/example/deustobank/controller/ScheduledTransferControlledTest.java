package com.example.deustobank.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.ScheduledTransfer;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.service.AlertService;
import com.example.deustobank.service.AuthService;
import com.example.deustobank.service.CategoryService;
import com.example.deustobank.service.ExportService;
import com.example.deustobank.service.NotificationService;
import com.example.deustobank.service.PdfService;
import com.example.deustobank.service.ScheduledTransferService;
import com.example.deustobank.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(controllers = ScheduledTransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduledTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- MockBeans requeridos por el contexto ---
    @MockBean private ScheduledTransferService service;
    @MockBean private AccountService accountService;
    @MockBean private AuthService authService;
    @MockBean private AlertService alertService;
    @MockBean private NotificationService notificationService;
    @MockBean private ExportService exportService;
    @MockBean private PdfService pdfService;
    @MockBean private UserService userService;
    @MockBean private CategoryService categoryService;
    @MockBean private UserRepository userRepository;
    @MockBean private TransactionRepository transactionRepository;
    @MockBean private AccountRepository accountRepository;

    private ScheduledTransfer testSt;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        Account from = new Account();
        from.setId(10L);
        from.setUser(user);
        from.setBalance(500.0);

        Account to = new Account();
        to.setId(20L);
        to.setUser(user);
        to.setBalance(100.0);

        testSt = new ScheduledTransfer();
        testSt.setFromAccount(from);
        testSt.setToAccount(to);
        testSt.setAmount(100.0);
        testSt.setFrequency(ScheduledTransfer.Frequency.MONTHLY);
        testSt.setStartDate(LocalDate.of(2024, 1, 1));
        testSt.setNextExecution(LocalDate.of(2024, 1, 1));
        testSt.setActive(true);
    }

    // ============================================================
    // POST /scheduled-transfers — create
    // ============================================================

    @Test
    void create_Success() throws Exception {
        when(service.create(eq(10L), eq(20L), eq(100.0),
                eq(ScheduledTransfer.Frequency.MONTHLY),
                any(LocalDate.class), eq(1L)))
                .thenReturn(testSt);

        mockMvc.perform(post("/scheduled-transfers")
                        .param("fromId", "10")
                        .param("toId", "20")
                        .param("amount", "100.0")
                        .param("frequency", "MONTHLY")
                        .param("startDate", "2024-01-01")
                        .param("requesterId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void create_SameAccount_Error() throws Exception {
        when(service.create(eq(10L), eq(10L), eq(100.0),
                any(ScheduledTransfer.Frequency.class),
                any(LocalDate.class), eq(1L)))
                .thenThrow(new RuntimeException("Las cuentas no pueden ser iguales"));

        mockMvc.perform(post("/scheduled-transfers")
                        .param("fromId", "10")
                        .param("toId", "10")
                        .param("amount", "100.0")
                        .param("frequency", "MONTHLY")
                        .param("startDate", "2024-01-01")
                        .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Las cuentas no pueden ser iguales"));
    }

    @Test
    void create_InvalidAmount_Error() throws Exception {
        when(service.create(eq(10L), eq(20L), eq(-50.0),
                any(ScheduledTransfer.Frequency.class),
                any(LocalDate.class), eq(1L)))
                .thenThrow(new RuntimeException("Importe inválido"));

        mockMvc.perform(post("/scheduled-transfers")
                        .param("fromId", "10")
                        .param("toId", "20")
                        .param("amount", "-50.0")
                        .param("frequency", "MONTHLY")
                        .param("startDate", "2024-01-01")
                        .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Importe inválido"));
    }

    @Test
    void create_InsufficientBalance_Error() throws Exception {
        when(service.create(any(), any(), eq(9999.0),
                any(ScheduledTransfer.Frequency.class),
                any(LocalDate.class), any()))
                .thenThrow(new RuntimeException("Saldo insuficiente para programar la transferencia"));

        mockMvc.perform(post("/scheduled-transfers")
                        .param("fromId", "10")
                        .param("toId", "20")
                        .param("amount", "9999.0")
                        .param("frequency", "MONTHLY")
                        .param("startDate", "2024-01-01")
                        .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Saldo insuficiente para programar la transferencia"));
    }

    // ============================================================
    // PUT /scheduled-transfers/{id} — edit
    // ============================================================

    @Test
    void edit_Success() throws Exception {
        when(service.edit(eq(1L), eq(200.0),
                eq(ScheduledTransfer.Frequency.WEEKLY),
                any(LocalDate.class), eq(1L)))
                .thenReturn(testSt);

        mockMvc.perform(put("/scheduled-transfers/1")
                        .param("amount", "200.0")
                        .param("frequency", "WEEKLY")
                        .param("nextExecution", "2024-02-01")
                        .param("requesterId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void edit_NotFound_Error() throws Exception {
        when(service.edit(eq(99L), any(Double.class),
                any(ScheduledTransfer.Frequency.class),
                any(LocalDate.class), any()))
                .thenThrow(new RuntimeException("Transferencia programada no encontrada"));

        mockMvc.perform(put("/scheduled-transfers/99")
                        .param("amount", "200.0")
                        .param("frequency", "WEEKLY")
                        .param("nextExecution", "2024-02-01")
                        .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transferencia programada no encontrada"));
    }

    @Test
    void edit_InvalidAmount_Error() throws Exception {
        when(service.edit(eq(1L), eq(0.0),
                any(ScheduledTransfer.Frequency.class),
                any(LocalDate.class), any()))
                .thenThrow(new RuntimeException("Importe inválido"));

        mockMvc.perform(put("/scheduled-transfers/1")
                        .param("amount", "0.0")
                        .param("frequency", "WEEKLY")
                        .param("nextExecution", "2024-02-01")
                        .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Importe inválido"));
    }

    // ============================================================
    // DELETE /scheduled-transfers/{id} — cancel
    // ============================================================

    @Test
    void cancel_Success() throws Exception {
        doNothing().when(service).cancel(eq(1L), eq(1L));

        mockMvc.perform(delete("/scheduled-transfers/1")
                        .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Transferencia programada cancelada"));
    }

    @Test
    void cancel_NotFound_Error() throws Exception {
        doThrow(new RuntimeException("Transferencia programada no encontrada"))
                .when(service).cancel(eq(99L), any());

        mockMvc.perform(delete("/scheduled-transfers/99")
                        .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transferencia programada no encontrada"));
    }

    // ============================================================
    // GET /scheduled-transfers/account/{accountId} — getByAccount
    // ============================================================

    @Test
    void getByAccount_Success() throws Exception {
        when(service.getByAccount(10L)).thenReturn(List.of(testSt));

        mockMvc.perform(get("/scheduled-transfers/account/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }

    @Test
    void getByAccount_Empty() throws Exception {
        when(service.getByAccount(10L)).thenReturn(List.of());

        mockMvc.perform(get("/scheduled-transfers/account/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}