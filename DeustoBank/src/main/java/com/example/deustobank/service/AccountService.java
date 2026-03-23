package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.deustobank.model.Account;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repo;
    
    @Autowired
    private TransactionRepository transactionRepo;

    public List<Account> getAll() {
        return repo.findAll();
    }

    public Account create(Account account) {
        return repo.save(account);
    }
    
    public Account deposit(Long id, double amount) {
        Account acc = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (amount <= 0) {
            throw new RuntimeException("Cantidad inválida");
        }

        double before = acc.getBalance();
        double after = before + amount;

        acc.setBalance(after);
        repo.save(acc);

        Transaction t = new Transaction("DEPOSIT", amount, acc);
        t.setBalanceBefore(before);
        t.setBalanceAfter(after);

        transactionRepo.save(t);

        return acc;
    }

    public Account withdraw(Long id, double amount) {
        Account acc = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (amount <= 0) {
            throw new RuntimeException("Cantidad inválida");
        }

        if (acc.getBalance() < amount) {
            throw new RuntimeException("Saldo insuficiente");
        }

        double before = acc.getBalance();
        double after = before - amount;

        acc.setBalance(after);
        repo.save(acc);

        Transaction t = new Transaction("WITHDRAW", amount, acc);
        t.setBalanceBefore(before);
        t.setBalanceAfter(after);

        transactionRepo.save(t);

        return acc;
    }
}