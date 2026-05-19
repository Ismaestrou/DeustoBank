package com.example.deustobank.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.ScheduledTransfer;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.ScheduledTransferRepository;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.service.ScheduledTransferService;

@ExtendWith(MockitoExtension.class)
class ScheduledTransferJobTest {

    @Mock private ScheduledTransferRepository repo;
    @Mock private AccountService accountService;
    @Mock private ScheduledTransferService scheduledTransferService;

    @InjectMocks
    private ScheduledTransferJob job;

    private User user;
    private Account fromAccount;
    private Account toAccount;
    private ScheduledTransfer st;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        fromAccount = new Account();
        fromAccount.setId(10L);
        fromAccount.setUser(user);
        fromAccount.setBalance(500.0);

        toAccount = new Account();
        toAccount.setId(20L);
        toAccount.setUser(user);
        toAccount.setBalance(100.0);

        st = new ScheduledTransfer();
        st.setFromAccount(fromAccount);
        st.setToAccount(toAccount);
        st.setAmount(50.0);
        st.setFrequency(ScheduledTransfer.Frequency.MONTHLY);
        st.setActive(true);
        st.setNextExecution(LocalDate.now().minusDays(1));
    }

    @Test
    void ejecutarTransferencias_NoPendingTransfers_DoesNothing() {
        when(repo.findByActiveTrueAndNextExecutionLessThanEqual(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> job.ejecutarTransferencias());

        verifyNoInteractions(accountService);
        verifyNoInteractions(scheduledTransferService);
    }

    @Test
    void ejecutarTransferencias_SuccessfulTransfer_UpdatesNextExecution() {
        LocalDate nextExec = LocalDate.now().plusMonths(1);
        when(repo.findByActiveTrueAndNextExecutionLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(st));
        when(accountService.transfer(10L, 20L, 50.0, 1L)).thenReturn(null);
        when(scheduledTransferService.calcularProximaEjecucion(
                st.getNextExecution(), st.getFrequency()))
                .thenReturn(nextExec);

        job.ejecutarTransferencias();

        verify(accountService).transfer(10L, 20L, 50.0, 1L);
        assertEquals(nextExec, st.getNextExecution());
        assertTrue(st.isActive());
        verify(repo).save(st);
    }

    @Test
    void ejecutarTransferencias_TransferFails_DeactivatesTransfer() {
        when(repo.findByActiveTrueAndNextExecutionLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(st));
        when(accountService.transfer(10L, 20L, 50.0, 1L))
                .thenThrow(new RuntimeException("Saldo insuficiente"));

        job.ejecutarTransferencias();

        assertFalse(st.isActive());
        verify(repo).save(st);
        verify(scheduledTransferService, never()).calcularProximaEjecucion(any(), any());
    }

    @Test
    void ejecutarTransferencias_MultipleTransfers_OneFailsOneSucceeds() {
        ScheduledTransfer st2 = new ScheduledTransfer();
        st2.setFromAccount(fromAccount);
        st2.setToAccount(toAccount);
        st2.setAmount(200.0);
        st2.setFrequency(ScheduledTransfer.Frequency.WEEKLY);
        st2.setActive(true);
        st2.setNextExecution(LocalDate.now().minusDays(1));

        when(repo.findByActiveTrueAndNextExecutionLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(st, st2));

        when(accountService.transfer(10L, 20L, 50.0, 1L)).thenReturn(null);
        when(accountService.transfer(10L, 20L, 200.0, 1L))
                .thenThrow(new RuntimeException("Limite superado"));

        LocalDate nextExec = LocalDate.now().plusMonths(1);
        when(scheduledTransferService.calcularProximaEjecucion(
                st.getNextExecution(), st.getFrequency()))
                .thenReturn(nextExec);

        job.ejecutarTransferencias();

        assertTrue(st.isActive());
        assertFalse(st2.isActive());
        verify(repo, times(2)).save(any(ScheduledTransfer.class));
    }

    @Test
    void ejecutarTransferencias_DailyFrequency_UpdatesNextDay() {
        st.setFrequency(ScheduledTransfer.Frequency.DAILY);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(repo.findByActiveTrueAndNextExecutionLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(st));
        when(accountService.transfer(any(), any(), anyDouble(), any())).thenReturn(null);
        when(scheduledTransferService.calcularProximaEjecucion(
                st.getNextExecution(), ScheduledTransfer.Frequency.DAILY))
                .thenReturn(tomorrow);

        job.ejecutarTransferencias();

        assertEquals(tomorrow, st.getNextExecution());
        assertTrue(st.isActive());
    }
}