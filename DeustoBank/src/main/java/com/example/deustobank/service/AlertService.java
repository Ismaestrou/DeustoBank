package com.example.deustobank.service;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.SuspiciousAlert;
import com.example.deustobank.model.SuspiciousAlert.AlertType;
import com.example.deustobank.repository.SuspiciousAlertRepository;
import com.example.deustobank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AlertService {

    private static final double HIGH_AMOUNT_THRESHOLD = 10_000.0;
    private static final long   FREQUENCY_MAX_TX      = 5;
    private static final int    FREQUENCY_WINDOW_MIN  = 10;

    @Autowired
    private SuspiciousAlertRepository alertRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    public void checkAndAlert(Account account, double amount) {
        checkHighAmount(account, amount);
        checkHighFrequency(account);
    }

    private void checkHighAmount(Account account, double amount) {
        if (amount < HIGH_AMOUNT_THRESHOLD) return;
        if (alertRepo.existsByAccountIdAndAlertTypeAndReviewedFalse(account.getId(), AlertType.HIGH_AMOUNT)) return;

        String desc = String.format(
            "Transacción de %.2f € detectada en la cuenta '%s' (ID %d). Supera el umbral de %.0f €.",
            amount, account.getOwnerName(), account.getId(), HIGH_AMOUNT_THRESHOLD
        );
        alertRepo.save(new SuspiciousAlert(AlertType.HIGH_AMOUNT, desc, account));
    }

    private void checkHighFrequency(Account account) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(FREQUENCY_WINDOW_MIN);
        long recentCount = transactionRepo.countRecentByAccountId(account.getId(), since);

        if (recentCount <= FREQUENCY_MAX_TX) return;
        if (alertRepo.existsByAccountIdAndAlertTypeAndReviewedFalse(account.getId(), AlertType.HIGH_FREQUENCY)) return;

        String desc = String.format(
            "%d transacciones en los últimos %d minutos en la cuenta '%s' (ID %d). Supera el límite de %d.",
            recentCount, FREQUENCY_WINDOW_MIN, account.getOwnerName(), account.getId(), FREQUENCY_MAX_TX
        );
        alertRepo.save(new SuspiciousAlert(AlertType.HIGH_FREQUENCY, desc, account));
    }
}