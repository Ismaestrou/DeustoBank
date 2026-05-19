package com.example.deustobank.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.SuspiciousAlertRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private SuspiciousAlertRepository alertRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        account1 = new Account();
        account1.setId(10L);
        account1.setUser(user);

        account2 = new Account();
        account2.setId(20L);
        account2.setUser(user);
    }

    // ============================================================
    // deleteUser — con cuentas asociadas
    // ============================================================

    @Test
    void deleteUser_WithAccounts_DeletesAllRelated() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(account1, account2));

        userService.deleteUser(1L);

        // verifica borrado de alertas y transacciones por cada cuenta
        verify(alertRepository).deleteByAccountId(10L);
        verify(transactionRepository).deleteByAccountId(10L);
        verify(alertRepository).deleteByAccountId(20L);
        verify(transactionRepository).deleteByAccountId(20L);

        // verifica borrado de cuentas y usuario
        verify(accountRepository).deleteByUserId(1L);
        verify(userRepository).deleteById(1L);
    }

    // ============================================================
    // deleteUser — sin cuentas asociadas
    // ============================================================

    @Test
    void deleteUser_NoAccounts_DeletesUser() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of());

        userService.deleteUser(1L);

        verify(alertRepository, never()).deleteByAccountId(anyLong());
        verify(transactionRepository, never()).deleteByAccountId(anyLong());
        verify(accountRepository).deleteByUserId(1L);
        verify(userRepository).deleteById(1L);
    }

    // ============================================================
    // deleteUser — usuario con una única cuenta
    // ============================================================

    @Test
    void deleteUser_OneAccount_DeletesRelated() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(account1));

        userService.deleteUser(1L);

        verify(alertRepository, times(1)).deleteByAccountId(10L);
        verify(transactionRepository, times(1)).deleteByAccountId(10L);
        verify(accountRepository).deleteByUserId(1L);
        verify(userRepository).deleteById(1L);
    }
}