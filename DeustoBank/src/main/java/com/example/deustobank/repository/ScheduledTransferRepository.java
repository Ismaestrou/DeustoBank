package com.example.deustobank.repository;

import com.example.deustobank.model.ScheduledTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, Long> {
    List<ScheduledTransfer> findByFromAccount_IdAndActiveTrue(Long accountId);
    List<ScheduledTransfer> findByActiveTrueAndNextExecutionLessThanEqual(LocalDate date);
}