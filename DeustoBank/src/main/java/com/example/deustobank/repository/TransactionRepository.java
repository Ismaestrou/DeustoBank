package com.example.deustobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.deustobank.model.Transaction;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
}