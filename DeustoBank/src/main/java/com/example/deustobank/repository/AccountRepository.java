package com.example.deustobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.deustobank.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
