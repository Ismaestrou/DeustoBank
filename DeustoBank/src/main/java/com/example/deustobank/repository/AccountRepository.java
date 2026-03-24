package com.example.deustobank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.deustobank.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
	List<Account> findByUserId(Long userId);
}