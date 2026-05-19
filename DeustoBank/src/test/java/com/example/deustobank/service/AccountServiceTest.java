package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.AccountResponse;
import com.example.deustobank.model.SystemStatsDTO;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.model.User;

import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionRepository transactionRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AlertService alertService;

    // =========================
    // MOCK FALTANTE
    // =========================

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
        user.setEmail("test@test.com");
        user.setDni("12345678A");

        account = new Account();
        account.setId(10L);
        account.setUser(user);
        account.setOwnerName("Test User");
        account.setBalance(100.0);
        account.setLimiteGastoMensual(0.0);
        account.setGastoMensualActual(0.0);

        account2 = new Account();
        account2.setId(20L);
        account2.setUser(user);
        account2.setOwnerName("Test User");
        account2.setBalance(50.0);
    }

    @Test
    void getAll_ReturnsList() {

        when(accountRepo.findAll())
                .thenReturn(List.of(account));

        List<Account> accounts = accountService.getAll();

        assertEquals(1, accounts.size());
    }

    @Test
    void getById_Success() {

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        Account result = accountService.getById(10L);

        assertEquals(10L, result.getId());
    }

    @Test
    void getById_NotFound() {

        when(accountRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> accountService.getById(99L)
        );
    }

    @Test
    void getAccountsByUser_ReturnsList() {

        when(accountRepo.findByUserId(1L))
                .thenReturn(List.of(account, account2));

        List<Account> accounts =
                accountService.getAccountsByUser(1L);

        assertEquals(2, accounts.size());
    }

    @Test
    void create_Success() {

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        when(accountRepo.save(any(Account.class)))
                .thenReturn(account);

        Account result = accountService.create(account, 1L);

        assertNotNull(result);
        assertEquals(user, result.getUser());
    }

    @Test
    void create_NegativeBalance() {

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        Account newAcc = new Account();
        newAcc.setBalance(-10.0);

        assertThrows(
                RuntimeException.class,
                () -> accountService.create(newAcc, 1L)
        );
    }

    @Test
    void deposit_Success() {

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        Account result =
                accountService.deposit(10L, 50.0, 1L);

        assertEquals(150.0, result.getBalance());

        verify(transactionRepo)
                .save(any(Transaction.class));

        verify(alertService)
                .checkAndAlert(eq(account), eq(50.0));

        verify(eventPublisher)
                .publishEvent(any());
    }

    @Test
    void deposit_InvalidAmount() {

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                RuntimeException.class,
                () -> accountService.deposit(10L, -10.0, 1L)
        );
    }

    @Test
    void withdraw_Success() {

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        AccountResponse result =
                accountService.withdraw(10L, 30.0, 1L);

        assertEquals(
                70.0,
                result.getAccount().getBalance()
        );

        assertNull(result.getAlert());

        verify(transactionRepo)
                .save(any(Transaction.class));

        verify(eventPublisher)
                .publishEvent(any());
    }

    @Test
    void withdraw_ExceedsLimit() {

        account.setLimiteGastoMensual(20.0);

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        Exception ex = assertThrows(
                RuntimeException.class,
                () -> accountService.withdraw(10L, 30.0, 1L)
        );

        assertTrue(
                ex.getMessage().contains("límite mensual")
        );
    }

    @Test
    void transfer_Success() {

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(accountRepo.findById(20L))
                .thenReturn(Optional.of(account2));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        String alert =
                accountService.transfer(10L, 20L, 40.0, 1L);

        assertEquals(60.0, account.getBalance());

        assertEquals(90.0, account2.getBalance());

        verify(transactionRepo, times(2))
                .save(any(Transaction.class));

        verify(eventPublisher, times(2))
                .publishEvent(any());

        assertNull(alert);
    }

    @Test
    void transfer_SameAccount() {

        assertThrows(
                RuntimeException.class,
                () -> accountService.transfer(10L, 10L, 50.0, 1L)
        );
    }

    @Test
    void deleteAccount_Success() {

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        when(transactionRepo.findByAccountIdOrderByDateDesc(10L))
                .thenReturn(List.of(new Transaction()));

        assertDoesNotThrow(
                () -> accountService.deleteAccount(10L, 1L)
        );

        verify(transactionRepo)
                .deleteAll(anyList());

        verify(accountRepo)
                .delete(account);
    }

    @Test
    void deleteAccount_NegativeBalance() {

        account.setBalance(-10.0);

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                RuntimeException.class,
                () -> accountService.deleteAccount(10L, 1L)
        );
    }

    @Test
    void getTotalBalanceByUser() {

        when(accountRepo.findByUserId(1L))
                .thenReturn(List.of(account, account2));

        double total =
                accountService.getTotalBalanceByUser(1L);

        assertEquals(150.0, total);
    }

    @Test
    void getSystemStats() {

        when(userRepo.count())
                .thenReturn(5L);

        when(transactionRepo.count())
                .thenReturn(100L);

        when(accountRepo.sumTotalBalance())
                .thenReturn(5000.0);

        SystemStatsDTO stats =
                accountService.getSystemStats();

        assertEquals(5L, stats.getTotalUsers());

        assertEquals(
                100L,
                stats.getTotalTransactions()
        );

        assertEquals(
                5000.0,
                stats.getTotalBalance()
        );
    }

    @Test
    void setLimiteGastoMensual_Success() {

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(accountRepo.save(any(Account.class)))
                .thenReturn(account);

        Account result =
                accountService.setLimiteGastoMensual(10L, 200.0);

        assertEquals(
                200.0,
                result.getLimiteGastoMensual()
        );
    }

    @Test
    void setUmbralSaldoBajo_Success() {

        when(accountRepo.findById(10L))
                .thenReturn(Optional.of(account));

        when(accountRepo.save(any(Account.class)))
                .thenReturn(account);

        Account result =
                accountService.setUmbralSaldoBajo(10L, 50.0);

        assertEquals(
                50.0,
                result.getUmbralSaldoBajo()
        );
    }
}