package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.deustobank.model.Account;
import com.example.deustobank.repository.AccountRepository;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repo;

    public List<Account> getAll() {
        return repo.findAll();
    }

    public Account create(Account account) {
        return repo.save(account);
    }
}