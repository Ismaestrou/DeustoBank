package com.example.deustobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.deustobank.model.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
    long countByAccountUserId(Long userId);
    List<Transaction> findByAccountIdAndDateBetween(Long accountId, LocalDateTime from, LocalDateTime to);
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId AND t.date >= :since")
    long countRecentByAccountId(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);
}