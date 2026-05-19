package com.example.deustobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.deustobank.model.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    //List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByAccountIdOrderByDateDesc(Long accountId);
    long countByAccountUserId(Long userId);
    List<Transaction> findByAccountIdAndDateBetween(Long accountId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId AND t.date >= :since")
    long countRecentByAccountId(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.account a JOIN FETCH a.user WHERE t.id = :id")
    Optional<Transaction> findByIdWithDetails(@Param("id") Long id);
}