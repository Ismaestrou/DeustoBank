package com.example.deustobank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.SuspiciousAlert;
import com.example.deustobank.model.SuspiciousAlert.AlertType;
import com.example.deustobank.repository.SuspiciousAlertRepository;
import com.example.deustobank.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private SuspiciousAlertRepository alertRepo;

    @Mock
    private TransactionRepository transactionRepo;

    @InjectMocks
    private AlertService alertService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(10L);
        account.setOwnerName("Test Owner");
    }

    @Test
    void checkAndAlert_NoAlerts_BelowThresholdAndFrequency() {
        when(transactionRepo.countRecentByAccountId(eq(10L), any(LocalDateTime.class))).thenReturn(2L);

        alertService.checkAndAlert(account, 5000.0);

        verify(alertRepo, never()).save(any(SuspiciousAlert.class));
    }

    @Test
    void checkAndAlert_HighAmount_CreatesAlert() {
        when(transactionRepo.countRecentByAccountId(eq(10L), any(LocalDateTime.class))).thenReturn(2L);
        when(alertRepo.existsByAccountIdAndAlertTypeAndReviewedFalse(10L, AlertType.HIGH_AMOUNT)).thenReturn(false);

        alertService.checkAndAlert(account, 15000.0);

        verify(alertRepo).save(argThat(alert -> alert.getAlertType() == AlertType.HIGH_AMOUNT));
    }

    @Test
    void checkAndAlert_HighAmount_AlreadyExists() {
        when(transactionRepo.countRecentByAccountId(eq(10L), any(LocalDateTime.class))).thenReturn(2L);
        when(alertRepo.existsByAccountIdAndAlertTypeAndReviewedFalse(10L, AlertType.HIGH_AMOUNT)).thenReturn(true);

        alertService.checkAndAlert(account, 15000.0);

        verify(alertRepo, never()).save(any(SuspiciousAlert.class));
    }

    @Test
    void checkAndAlert_HighFrequency_CreatesAlert() {
        when(transactionRepo.countRecentByAccountId(eq(10L), any(LocalDateTime.class))).thenReturn(10L);
        when(alertRepo.existsByAccountIdAndAlertTypeAndReviewedFalse(10L, AlertType.HIGH_FREQUENCY)).thenReturn(false);

        alertService.checkAndAlert(account, 100.0);

        verify(alertRepo).save(argThat(alert -> alert.getAlertType() == AlertType.HIGH_FREQUENCY));
    }

    @Test
    void checkAndAlert_HighFrequency_AlreadyExists() {
        when(transactionRepo.countRecentByAccountId(eq(10L), any(LocalDateTime.class))).thenReturn(10L);
        when(alertRepo.existsByAccountIdAndAlertTypeAndReviewedFalse(10L, AlertType.HIGH_FREQUENCY)).thenReturn(true);

        alertService.checkAndAlert(account, 100.0);

        verify(alertRepo, never()).save(any(SuspiciousAlert.class));
    }
}
