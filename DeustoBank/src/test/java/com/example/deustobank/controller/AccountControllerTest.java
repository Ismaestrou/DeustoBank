package com.example.deustobank.controller;

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
        when(accountService.deposit(eq(10L), eq(50.0), eq(1L))).thenReturn(account);

        mockMvc.perform(put("/accounts/10/deposit")
                .param("amount", "50.0")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deposit_BadRequest() throws Exception {
        when(accountService.deposit(anyLong(), anyDouble(), anyLong())).thenThrow(new RuntimeException("Error deposit"));

        mockMvc.perform(put("/accounts/10/deposit")
                .param("amount", "50.0")
                .param("requesterId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error deposit"));
    }

    @Test
    void withdraw_Success() throws Exception {
        AccountResponse response = new AccountResponse(account, null);
        when(accountService.withdraw(eq(10L), eq(20.0), eq(1L))).thenReturn(response);

        mockMvc.perform(put("/accounts/10/withdraw")
                .param("amount", "20.0")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.id").value(10));
    }

    @Test
    void withdraw_BadRequest() throws Exception {
        when(accountService.withdraw(anyLong(), anyDouble(), anyLong())).thenThrow(new RuntimeException("Error withdraw"));

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
        when(transactionRepo.findByAccountId(10L)).thenReturn(List.of(t));

        mockMvc.perform(get("/accounts/10/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }
}
