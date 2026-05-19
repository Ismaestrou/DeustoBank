package com.example.deustobank.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.AccountResponse;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionRepository transactionRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(10L);
        account.setBalance(100.0);
    }

    @Test
    void getAll_Success() throws Exception {
        when(accountService.getAll()).thenReturn(List.of(account));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getById_Success() throws Exception {
        when(accountService.getById(10L)).thenReturn(account);

        mockMvc.perform(get("/accounts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getAccountsByUser_Success() throws Exception {
        when(accountService.getAccountsByUser(1L)).thenReturn(List.of(account));

        mockMvc.perform(get("/accounts/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void deposit_Success() throws Exception {
        when(accountService.deposit(eq(10L), eq(50.0), eq(1L), any())).thenReturn(account);

        mockMvc.perform(put("/accounts/10/deposit")
                .param("amount", "50.0")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deposit_BadRequest() throws Exception {
        when(accountService.deposit(anyLong(), anyDouble(), anyLong(), any())).thenThrow(new RuntimeException("Error deposit"));

        mockMvc.perform(put("/accounts/10/deposit")
                .param("amount", "50.0")
                .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error deposit"));
    }

    @Test
    void withdraw_Success() throws Exception {
        AccountResponse response = new AccountResponse(account, null);
        when(accountService.withdraw(eq(10L), eq(20.0), eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/accounts/10/withdraw")
                .param("amount", "20.0")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.id").value(10));
    }

    @Test
    void withdraw_BadRequest() throws Exception {
        when(accountService.withdraw(anyLong(), anyDouble(), anyLong(), any())).thenThrow(new RuntimeException("Error withdraw"));

        mockMvc.perform(put("/accounts/10/withdraw")
                .param("amount", "20.0")
                .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error withdraw"));
    }

    @Test
    void transfer_Success() throws Exception {
        when(accountService.transfer(eq(10L), eq(20L), eq(30.0), eq(1L))).thenReturn(null);

        mockMvc.perform(post("/accounts/transfer")
                .param("fromId", "10")
                .param("toId", "20")
                .param("amount", "30.0")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Transferencia realizada"));
    }

    @Test
    void transfer_WithAlert() throws Exception {
        when(accountService.transfer(eq(10L), eq(20L), eq(30.0), eq(1L))).thenReturn("Saldo bajo alert");

        mockMvc.perform(post("/accounts/transfer")
                .param("fromId", "10")
                .param("toId", "20")
                .param("amount", "30.0")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transferencia realizada"))
                .andExpect(jsonPath("$.alert").value("Saldo bajo alert"));
    }

    @Test
    void setLimite_Success() throws Exception {
        when(accountService.setLimiteGastoMensual(10L, 500.0)).thenReturn(account);

        mockMvc.perform(put("/accounts/10/limite")
                .param("limite", "500.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void setUmbral_Success() throws Exception {
        when(accountService.setUmbralSaldoBajo(10L, 50.0)).thenReturn(account);

        mockMvc.perform(put("/accounts/10/umbral")
                .param("umbral", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getTransactions_Success() throws Exception {
        Transaction t = new Transaction();
        t.setId(100L);
        when(transactionRepo.findByAccountIdOrderByDateDesc(10L)).thenReturn(List.of(t));

        mockMvc.perform(get("/accounts/10/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void delete_Success() throws Exception {

        doNothing().when(accountService).deleteAccount(10L, 1L);

        mockMvc.perform(delete("/accounts/10")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cuenta eliminada correctamente"));
    }

    @Test
    void transfer_Error() throws Exception {

        when(accountService.transfer(anyLong(), anyLong(), anyDouble(), anyLong()))
                .thenThrow(new RuntimeException("Error transferencia"));

        mockMvc.perform(post("/accounts/transfer")
                .param("fromId", "10")
                .param("toId", "20")
                .param("amount", "30")
                .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error transferencia"));
    }

    @Test
    void create_Success() throws Exception {

        when(accountService.create(any(Account.class), eq(1L)))
                .thenReturn(account);

        mockMvc.perform(post("/accounts")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getTransactionById_Success() throws Exception {

        Transaction t = new Transaction();
        t.setId(100L);

        when(transactionRepo.findByIdWithDetails(100L))
                .thenReturn(Optional.of(t));

        mockMvc.perform(get("/accounts/transactions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void getTransactionById_NotFound() throws Exception {

        when(transactionRepo.findByIdWithDetails(100L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/accounts/transactions/100"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactions_WithDates() throws Exception {

        Transaction t = new Transaction();
        t.setId(200L);

        when(transactionRepo.findByAccountIdAndDateBetween(
                eq(10L),
                any(),
                any()))
                .thenReturn(List.of(t));

        mockMvc.perform(get("/accounts/10/transactions")
                .param("from", "2026-01-01")
                .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(200));
    }

}
