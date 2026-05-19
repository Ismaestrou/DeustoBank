package com.example.deustobank.repository;

import com.example.deustobank.model.SuspiciousAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SuspiciousAlertRepository extends JpaRepository<SuspiciousAlert, Long> {

    List<SuspiciousAlert> findAllByOrderByCreatedAtDesc();

    List<SuspiciousAlert> findByReviewedFalseOrderByCreatedAtDesc();

    boolean existsByAccountIdAndAlertTypeAndReviewedFalse(
        Long accountId,
        SuspiciousAlert.AlertType alertType
    );

    void deleteByAccountId(Long accountId);
}