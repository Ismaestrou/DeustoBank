package com.example.deustobank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.deustobank.model.Account;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Long> {
	List<Account> findByUserId(Long userId);
	@Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
	double sumTotalBalance();
}