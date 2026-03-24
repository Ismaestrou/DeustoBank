package com.example.deustobank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.service.AccountService;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService service;

    @GetMapping
    public List<Account> getAll() {
        return service.getAll();
    }
    
    @Autowired
    private TransactionRepository transactionRepo;

    @GetMapping("/{id}/transactions")
    public List<Transaction> getTransactions(@PathVariable Long id) {
        return transactionRepo.findByAccountId(id);
    }

    @PostMapping
    public Account create(@Valid @RequestBody Account account) {
        return service.create(account);
    }
    
    @PutMapping("/{id}/deposit")
    public Account deposit(@PathVariable Long id, @RequestParam double amount) {
        return service.deposit(id, amount);
    }

    @PutMapping("/{id}/withdraw")
    public Account withdraw(@PathVariable Long id, @RequestParam double amount) {
        return service.withdraw(id, amount);
    }
    @GetMapping("/{id}")
    public Account getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteAccount(id);
        return "Cuenta con ID " + id + " eliminada correctamente.";
    }
}