package com.example.deustobank.service;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.AccountResponse;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;
import com.example.deustobank.service.AccountService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionRepository transactionRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AccountService service;

    private Account account;
    private User user;

    private void setId(Object obj, Long id) {
        try {
            Field field = obj.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        setId(user, 1L);
        user.setRole("USER");
        user.setActive(true);

        account = new Account();
        setId(account, 1L);
        account.setBalance(100);
        account.setUser(user);
    }

    // Verifica que al realizar un depósito válido, el saldo de la cuenta aumenta correctamente
    @Test
    void deposit_shouldIncreaseBalance() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        Account result = service.deposit(1L, 50, 1L);

        assertEquals(150, result.getBalance());
        verify(accountRepo).save(account);
    }

    // Verifica que al realizar una retirada válida, el saldo de la cuenta disminuye correctamente
    @Test
    void withdraw_shouldDecreaseBalance() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        AccountResponse result = service.withdraw(1L, 50, 1L);

        assertEquals(50, result.getAccount().getBalance());
        verify(accountRepo).save(account);
    }

    @Test
    void withdraw_invalidAmount_shouldThrowException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> service.withdraw(1L, 0, 1L));
    }

    @Test
    void withdraw_exceedsMonthlyLimit_shouldThrowException() {
        account.setLimiteGastoMensual(30);
        account.setGastoMensualActual(20);

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> service.withdraw(1L, 20, 1L));
    }

    @Test
    void withdraw_belowThreshold_shouldReturnAlert() {
        account.setUmbralSaldoBajo(80.0);

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        AccountResponse result = service.withdraw(1L, 50, 1L);

        assertNotNull(result.getAlert());
        assertTrue(result.getAlert().contains("Saldo bajo"));
    }

    // Verifica que al intentar depositar una cantidad inválida, se lance una excepción
    @Test
    void deposit_invalidAmount_shouldThrowException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> {
            service.deposit(1L, -10, 1L);
        });
    }

    @Test
    void deposit_zeroAmount_shouldThrowException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> service.deposit(1L, 0, 1L));
    }

    // Verifica que un usuario no puede realizar operaciones sobre una cuenta que no le pertenece (control de acceso)
    @Test
    void access_otherUserAccount_shouldFail() {
        User anotherUser = new User();
        anotherUser.setRole("USER");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(2L)).thenReturn(Optional.of(anotherUser));

        assertThrows(RuntimeException.class, () -> {
            service.deposit(1L, 50, 2L);
        });
    }

    // Verifica que un administrador puede operar sobre cualquier cuenta, independientemente del propietario (control de roles)
    @Test
    void admin_shouldAccessAnyAccount() {

        User admin = new User();
        admin.setRole("ADMIN");

        account.setUser(user); // cuenta pertenece al user normal

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(2L)).thenReturn(Optional.of(admin));

        Account result = service.deposit(1L, 50, 2L);

        assertEquals(150, result.getBalance());
    }

    // Verifica nuevamente que un usuario distinto al propietario no puede acceder a una cuenta ajena (seguridad reforzada)
    @Test
    void user_shouldNotAccessOtherAccount() {

        User anotherUser = new User();
        anotherUser.setRole("USER");

        account.setUser(user); // cuenta del user 1

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(2L)).thenReturn(Optional.of(anotherUser));

        assertThrows(RuntimeException.class, () -> {
            service.deposit(1L, 50, 2L);
        });
    }

    // Verifica que un usuario bloqueado no puede realizar operaciones sobre su cuenta (estado del usuario)
    @Test
    void blockedUser_shouldNotOperate() {

        user.setActive(false); // usuario bloqueado

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> {
            service.deposit(1L, 50, 1L);
        });
    }

    // Verifica que un usuario activo puede realizar operaciones correctamente sobre su propia cuenta
    @Test
    void user_shouldOperateOwnAccount() {

        account.setUser(user);

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        Account result = service.deposit(1L, 50, 1L);

        assertEquals(150, result.getBalance());
    }

    @Test
    void transfer_shouldUpdateBothBalances() {
        Account destino = new Account();
        setId(destino, 2L);
        destino.setBalance(50);
        destino.setUser(user);

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(destino));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        service.transfer(1L, 2L, 30, 1L);

        assertEquals(70, account.getBalance());
        assertEquals(80, destino.getBalance());
        verify(accountRepo).save(account);
        verify(accountRepo).save(destino);
    }

    @Test
    void transfer_sameAccount_shouldThrowException() {
        assertThrows(RuntimeException.class, () -> service.transfer(1L, 1L, 30, 1L));
    }

    @Test
    void transfer_exceedsMonthlyLimit_shouldThrowException() {
        account.setLimiteGastoMensual(20);
        account.setGastoMensualActual(10);

        Account destino = new Account();
        setId(destino, 2L);
        destino.setBalance(50);
        destino.setUser(user);

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(destino));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> service.transfer(1L, 2L, 30, 1L));
    }

    @Test
    void setLimiteGastoMensual_shouldUpdateLimit() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepo.save(account)).thenReturn(account);

        Account result = service.setLimiteGastoMensual(1L, 200);

        assertEquals(200, result.getLimiteGastoMensual());
        verify(accountRepo).save(account);
    }

    @Test
    void setLimiteGastoMensual_negativeValue_shouldThrowException() {
        assertThrows(RuntimeException.class, () -> service.setLimiteGastoMensual(1L, -50));
    }

    @Test
    void setUmbralSaldoBajo_shouldUpdateThreshold() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepo.save(account)).thenReturn(account);

        Account result = service.setUmbralSaldoBajo(1L, 10);

        assertEquals(10, result.getUmbralSaldoBajo());
        verify(accountRepo).save(account);
    }

    @Test
    void setUmbralSaldoBajo_negativeValue_shouldThrowException() {
        assertThrows(RuntimeException.class, () -> service.setUmbralSaldoBajo(1L, -10));
    }
}