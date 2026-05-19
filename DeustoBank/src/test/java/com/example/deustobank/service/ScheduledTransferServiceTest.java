package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.ScheduledTransfer;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.ScheduledTransferRepository;

@ExtendWith(MockitoExtension.class)
class ScheduledTransferServiceTest {

    @Mock private ScheduledTransferRepository repo;
    @Mock private AccountRepository accountRepo;
    @Mock private AccountService accountService;

    @InjectMocks
    private ScheduledTransferService service;

    private Account from;
    private Account to;
    private ScheduledTransfer st;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        from = new Account();
        from.setId(10L);
        from.setUser(user);
        from.setBalance(500.0);

        to = new Account();
        to.setId(20L);
        to.setUser(user);
        to.setBalance(100.0);

        st = new ScheduledTransfer();
        st.setFromAccount(from);
        st.setToAccount(to);
        st.setAmount(100.0);
        st.setFrequency(ScheduledTransfer.Frequency.MONTHLY);
        st.setStartDate(LocalDate.of(2024, 1, 1));
        st.setNextExecution(LocalDate.of(2024, 1, 1));
        st.setActive(true);
    }

    // ============================================================
    // create — éxito
    // ============================================================

    @Test
    void create_Success() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(from));
        when(accountRepo.findById(20L)).thenReturn(Optional.of(to));
        when(repo.save(any(ScheduledTransfer.class))).thenReturn(st);

        ScheduledTransfer result = service.create(10L, 20L, 100.0,
                ScheduledTransfer.Frequency.MONTHLY,
                LocalDate.of(2024, 1, 1), 1L);

        assertNotNull(result);
        assertEquals(100.0, result.getAmount());
        verify(repo).save(any(ScheduledTransfer.class));
    }

    @Test
    void create_FromAccountNotFound_Throws() {
        when(accountRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.create(99L, 20L, 100.0, ScheduledTransfer.Frequency.MONTHLY,
                        LocalDate.of(2024, 1, 1), 1L));
    }

    @Test
    void create_ToAccountNotFound_Throws() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(from));
        when(accountRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.create(10L, 99L, 100.0, ScheduledTransfer.Frequency.MONTHLY,
                        LocalDate.of(2024, 1, 1), 1L));
    }

    @Test
    void create_SameAccount_Throws() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(from));

        assertThrows(RuntimeException.class, () ->
                service.create(10L, 10L, 100.0, ScheduledTransfer.Frequency.MONTHLY,
                        LocalDate.of(2024, 1, 1), 1L));
    }

    @Test
    void create_InvalidAmount_Throws() {
        when(accountRepo.findById(10L)).thenReturn(Optional.of(from));
        when(accountRepo.findById(20L)).thenReturn(Optional.of(to));

        assertThrows(RuntimeException.class, () ->
                service.create(10L, 20L, -50.0, ScheduledTransfer.Frequency.MONTHLY,
                        LocalDate.of(2024, 1, 1), 1L));
    }

    @Test
    void create_InsufficientBalance_Throws() {
        from.setBalance(10.0);
        when(accountRepo.findById(10L)).thenReturn(Optional.of(from));
        when(accountRepo.findById(20L)).thenReturn(Optional.of(to));

        assertThrows(RuntimeException.class, () ->
                service.create(10L, 20L, 500.0, ScheduledTransfer.Frequency.MONTHLY,
                        LocalDate.of(2024, 1, 1), 1L));
    }

    // ============================================================
    // edit — éxito y errores
    // ============================================================

    @Test
    void edit_Success() {
        when(repo.findById(1L)).thenReturn(Optional.of(st));
        when(repo.save(any(ScheduledTransfer.class))).thenReturn(st);

        ScheduledTransfer result = service.edit(1L, 200.0,
                ScheduledTransfer.Frequency.WEEKLY,
                LocalDate.of(2024, 2, 1), 1L);

        assertEquals(200.0, result.getAmount());
        assertEquals(ScheduledTransfer.Frequency.WEEKLY, result.getFrequency());
        verify(repo).save(st);
    }

    @Test
    void edit_NotFound_Throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.edit(99L, 200.0, ScheduledTransfer.Frequency.WEEKLY,
                        LocalDate.of(2024, 2, 1), 1L));
    }

    @Test
    void edit_InvalidAmount_Throws() {
        when(repo.findById(1L)).thenReturn(Optional.of(st));

        assertThrows(RuntimeException.class, () ->
                service.edit(1L, 0.0, ScheduledTransfer.Frequency.WEEKLY,
                        LocalDate.of(2024, 2, 1), 1L));
    }

    // ============================================================
    // cancel — éxito y no encontrado
    // ============================================================

    @Test
    void cancel_Success() {
        when(repo.findById(1L)).thenReturn(Optional.of(st));
        when(repo.save(any(ScheduledTransfer.class))).thenReturn(st);

        service.cancel(1L, 1L);

        assertFalse(st.isActive());
        verify(repo).save(st);
    }

    @Test
    void cancel_NotFound_Throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.cancel(99L, 1L));
    }

    // ============================================================
    // getByAccount
    // ============================================================

    @Test
    void getByAccount_ReturnsList() {
        when(repo.findByFromAccount_IdAndActiveTrue(10L)).thenReturn(List.of(st));

        List<ScheduledTransfer> result = service.getByAccount(10L);

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getAmount());
    }

    @Test
    void getByAccount_Empty() {
        when(repo.findByFromAccount_IdAndActiveTrue(10L)).thenReturn(List.of());

        List<ScheduledTransfer> result = service.getByAccount(10L);

        assertTrue(result.isEmpty());
    }

    // ============================================================
    // calcularProximaEjecucion — las 3 frecuencias
    // ============================================================

    @Test
    void calcularProximaEjecucion_Daily() {
        LocalDate base = LocalDate.of(2024, 1, 1);
        LocalDate result = service.calcularProximaEjecucion(base, ScheduledTransfer.Frequency.DAILY);
        assertEquals(LocalDate.of(2024, 1, 2), result);
    }

    @Test
    void calcularProximaEjecucion_Weekly() {
        LocalDate base = LocalDate.of(2024, 1, 1);
        LocalDate result = service.calcularProximaEjecucion(base, ScheduledTransfer.Frequency.WEEKLY);
        assertEquals(LocalDate.of(2024, 1, 8), result);
    }

    @Test
    void calcularProximaEjecucion_Monthly() {
        LocalDate base = LocalDate.of(2024, 1, 1);
        LocalDate result = service.calcularProximaEjecucion(base, ScheduledTransfer.Frequency.MONTHLY);
        assertEquals(LocalDate.of(2024, 2, 1), result);
    }
}