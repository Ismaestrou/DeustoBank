package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.AccountResponse;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.model.User;
import com.example.deustobank.model.SystemStatsDTO;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepo;
    @Mock private TransactionRepository transactionRepo;
    @Mock private UserRepository userRepo;
    @Mock private AlertService alertService;
    @Mock private CategoryService categoryService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;
    private Account account2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setRole("USER");
        user.setActive(true);

        account = new Account();
        account.setId(10L);
        account.setUser(user);
        account.setBalance(100.0);
        account.setLimiteGastoMensual(0.0);
        account.setGastoMensualActual(0.0);
        account.setUmbralSaldoBajo(0.0);

        account2 = new Account();
        account2.setId(20L);
        account2.setUser(user);
        account2.setBalance(50.0);
        account2.setLimiteGastoMensual(0.0);
        account2.setGastoMensualActual(0.0);
        account2.setUmbralSaldoBajo(0.0);
    }

    // ── getAll / getById ──────────────────────────────────────────────────────

    @Test
    void getAll_ReturnsList() {
        when(accountRepo.findAll()).thenReturn(List.of(account));
        assertEquals(1, accountService.getAll().size());
    }

    @Test
    void getById_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        assertEquals(10L, accountService.getById(10L).getId());
    }

    @Test
    void getById_NotFound() {
        when(accountRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> accountService.getById(99L));
    }

    @Test
    void getAccountsByUser_ReturnsList() {
        when(accountRepo.findByUserId(1L)).thenReturn(List.of(account, account2));
        assertEquals(2, accountService.getAccountsByUser(1L).size());
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepo.save(any(Account.class))).thenReturn(account);
        Account result = accountService.create(account, 1L);
        assertNotNull(result);
        assertEquals(user, result.getUser());
    }

    @Test
    void create_UserNotFound() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> accountService.create(account, 99L));
    }

    @Test
    void create_NegativeBalance() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        Account newAcc = new Account();
        newAcc.setBalance(-10.0);
        assertThrows(RuntimeException.class, () -> accountService.create(newAcc, 1L));
    }

    // ── deposit ───────────────────────────────────────────────────────────────

    @Test
    void deposit_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        Account result = accountService.deposit(10L, 50.0, 1L, null);

        assertEquals(150.0, result.getBalance());
        verify(transactionRepo).save(any(Transaction.class));
        verify(alertService).checkAndAlert(eq(account), eq(50.0));
    }

    @Test
    void deposit_InvalidAmount_Zero() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class, () -> accountService.deposit(10L, 0.0, 1L, null));
    }

    @Test
    void deposit_InvalidAmount_Negative() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class, () -> accountService.deposit(10L, -10.0, 1L, null));
    }

    @Test
    void deposit_BlockedUser_ThrowsException() {
        user.setActive(false);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));

        Exception ex = assertThrows(RuntimeException.class, () -> accountService.deposit(10L, 50.0, 1L, null));
        assertTrue(ex.getMessage().contains("bloqueado"));
    }

    @Test
    void deposit_Unauthorized_ThrowsException() {
        User other = new User();
        other.setId(2L);
        other.setRole("USER");
        other.setActive(true);

        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(2L)).thenReturn(Optional.of(other));

        assertThrows(RuntimeException.class, () -> accountService.deposit(10L, 50.0, 2L, null));
    }

    @Test
    void deposit_RequesterNotFound_ThrowsException() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountService.deposit(10L, 50.0, 99L, null));
    }

    @Test
    void deposit_AdminCanDepositToAnyAccount() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole("ADMIN");
        admin.setActive(true);

        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(99L)).thenReturn(Optional.of(admin));

        Account result = accountService.deposit(10L, 20.0, 99L, "concepto");
        assertEquals(120.0, result.getBalance());
    }

    // ── withdraw ──────────────────────────────────────────────────────────────

    @Test
    void withdraw_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        AccountResponse result = accountService.withdraw(10L, 30.0, 1L, null);
        assertEquals(70.0, result.getAccount().getBalance());
        assertNull(result.getAlert());
        verify(transactionRepo).save(any(Transaction.class));
    }

    @Test
    void withdraw_InvalidAmount_Zero() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class, () -> accountService.withdraw(10L, 0.0, 1L, null));
    }

    @Test
    void withdraw_ExceedsMonthlyLimit() {
        account.setLimiteGastoMensual(20.0);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        Exception ex = assertThrows(RuntimeException.class, () -> accountService.withdraw(10L, 30.0, 1L, null));
        assertTrue(ex.getMessage().contains("límite mensual"));
    }

    @Test
    void withdraw_WithMonthlyLimit_WithinLimit_Success() {
        account.setLimiteGastoMensual(100.0);
        account.setGastoMensualActual(10.0);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        AccountResponse result = accountService.withdraw(10L, 50.0, 1L, null);
        assertEquals(50.0, result.getAccount().getBalance());
        assertEquals(60.0, account.getGastoMensualActual());
    }

    @Test
    void withdraw_LowBalanceAlert_ReturnedInResponse() {
        account.setUmbralSaldoBajo(50.0);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        AccountResponse result = accountService.withdraw(10L, 60.0, 1L, null);

        assertEquals(40.0, result.getAccount().getBalance());
        assertNotNull(result.getAlert());
        assertTrue(result.getAlert().contains("Saldo bajo"));
    }

    @Test
    void withdraw_BlockedUser_ThrowsException() {
        user.setActive(false);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> accountService.withdraw(10L, 10.0, 1L, null));
    }

    // ── transfer ──────────────────────────────────────────────────────────────

    @Test
    void transfer_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepo.findById(20L)).thenReturn(Optional.of(account2));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        String alert = accountService.transfer(10L, 20L, 40.0, 1L);

        assertEquals(60.0, account.getBalance());
        assertEquals(90.0, account2.getBalance());
        verify(transactionRepo, times(2)).save(any(Transaction.class));
        assertNull(alert);
    }

    @Test
    void transfer_SameAccount_ThrowsException() {
        assertThrows(RuntimeException.class, () -> accountService.transfer(10L, 10L, 50.0, 1L));
    }

    @Test
    void transfer_InvalidAmount_Zero_ThrowsException() {
        assertThrows(RuntimeException.class, () -> accountService.transfer(10L, 20L, 0.0, 1L));
    }

    @Test
    void transfer_InvalidAmount_Negative_ThrowsException() {
        assertThrows(RuntimeException.class, () -> accountService.transfer(10L, 20L, -10.0, 1L));
    }

    @Test
    void transfer_ExceedsMonthlyLimit_ThrowsException() {
        account.setLimiteGastoMensual(30.0);
        account.setGastoMensualActual(0.0);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepo.findById(20L)).thenReturn(Optional.of(account2));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        Exception ex = assertThrows(RuntimeException.class, () -> accountService.transfer(10L, 20L, 50.0, 1L));
        assertTrue(ex.getMessage().contains("límite mensual"));
    }

    @Test
    void transfer_WithMonthlyLimit_WithinLimit_UpdatesGasto() {
        account.setLimiteGastoMensual(100.0);
        account.setGastoMensualActual(20.0);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepo.findById(20L)).thenReturn(Optional.of(account2));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        accountService.transfer(10L, 20L, 40.0, 1L);
        assertEquals(60.0, account.getGastoMensualActual());
    }

    @Test
    void transfer_LowBalanceAlert_ReturnedAsString() {
        account.setUmbralSaldoBajo(80.0);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepo.findById(20L)).thenReturn(Optional.of(account2));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        String alert = accountService.transfer(10L, 20L, 30.0, 1L);
        assertNotNull(alert);
        assertTrue(alert.contains("Saldo bajo"));
    }

    @Test
    void transfer_FromBlockedUser_ThrowsException() {
        user.setActive(false);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> accountService.transfer(10L, 20L, 10.0, 1L));
    }

    // ── deleteAccount ─────────────────────────────────────────────────────────

    @Test
    void deleteAccount_Success_WithTransactions() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepo.findByAccountIdOrderByDateDesc(10L)).thenReturn(List.of(new Transaction()));

        assertDoesNotThrow(() -> accountService.deleteAccount(10L, 1L));
        verify(transactionRepo).deleteAll(anyList());
        verify(accountRepo).delete(account);
    }

    @Test
    void deleteAccount_Success_NoTransactions() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepo.findByAccountIdOrderByDateDesc(10L)).thenReturn(List.of());

        assertDoesNotThrow(() -> accountService.deleteAccount(10L, 1L));
        verify(transactionRepo, never()).deleteAll(anyList());
        verify(accountRepo).delete(account);
    }

    @Test
    void deleteAccount_NegativeBalance_ThrowsException() {
        account.setBalance(-10.0);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> accountService.deleteAccount(10L, 1L));
    }

    @Test
    void deleteAccount_Unauthorized_ThrowsException() {
        User other = new User();
        other.setId(2L);
        other.setRole("USER");
        other.setActive(true);

        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(userRepo.findById(2L)).thenReturn(Optional.of(other));

        assertThrows(RuntimeException.class, () -> accountService.deleteAccount(10L, 2L));
    }

    // ── setLimiteGastoMensual / setUmbralSaldoBajo ────────────────────────────

    @Test
    void setLimiteGastoMensual_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        Account result = accountService.setLimiteGastoMensual(10L, 200.0);
        assertEquals(200.0, result.getLimiteGastoMensual());
    }

    @Test
    void setLimiteGastoMensual_Zero_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        assertDoesNotThrow(() -> accountService.setLimiteGastoMensual(10L, 0.0));
    }

    @Test
    void setLimiteGastoMensual_Negative_ThrowsException() {
        Exception ex = assertThrows(RuntimeException.class,
                () -> accountService.setLimiteGastoMensual(10L, -1.0));
        assertTrue(ex.getMessage().contains("negativo"));
    }

    @Test
    void setUmbralSaldoBajo_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        Account result = accountService.setUmbralSaldoBajo(10L, 50.0);
        assertEquals(50.0, result.getUmbralSaldoBajo());
    }

    @Test
    void setUmbralSaldoBajo_Zero_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        assertDoesNotThrow(() -> accountService.setUmbralSaldoBajo(10L, 0.0));
    }

    @Test
    void setUmbralSaldoBajo_Negative_ThrowsException() {
        Exception ex = assertThrows(RuntimeException.class,
                () -> accountService.setUmbralSaldoBajo(10L, -5.0));
        assertTrue(ex.getMessage().contains("negativo"));
    }

    // ── getTotalBalanceByUser / getSystemStats ────────────────────────────────

    @Test
    void getTotalBalanceByUser() {
        when(accountRepo.findByUserId(1L)).thenReturn(List.of(account, account2));
        assertEquals(150.0, accountService.getTotalBalanceByUser(1L));
    }

    @Test
    void getTotalBalanceByUser_NoAccounts_ReturnsZero() {
        when(accountRepo.findByUserId(99L)).thenReturn(List.of());
        assertEquals(0.0, accountService.getTotalBalanceByUser(99L));
    }

    @Test
    void getSystemStats() {
        when(userRepo.count()).thenReturn(5L);
        when(transactionRepo.count()).thenReturn(100L);
        when(accountRepo.sumTotalBalance()).thenReturn(5000.0);

        SystemStatsDTO stats = accountService.getSystemStats();
        assertEquals(5L, stats.getTotalUsers());
        assertEquals(100L, stats.getTotalTransactions());
        assertEquals(5000.0, stats.getTotalBalance());
    }
}