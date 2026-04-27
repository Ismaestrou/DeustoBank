package com.example.deustobank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.repository.TransactionRepository;

@RestController
@RequestMapping("/accounts")
@CrossOrigin
public class AccountController {

    @Autowired
    private AccountService service;

    @Autowired
    private TransactionRepository transactionRepo;

    // 🔹 ADMIN (deberías protegerlo también si quieres nota alta)
    @GetMapping
    public List<Account> getAll() {
        return service.getAll();
    }

    // 🔹 Usuario ve sus cuentas
    @GetMapping("/user/{userId}")
    public List<Account> getByUser(@PathVariable Long userId) {
        return service.getAccountsByUser(userId);
    }

    @GetMapping("/{id}")
    public Account getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/transactions")
public List<Transaction> getTransactions(
        @PathVariable Long id,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to) {

    if (from != null && to != null) {
        LocalDateTime fromDate = LocalDate.parse(from).atStartOfDay();
        LocalDateTime toDate   = LocalDate.parse(to).atTime(23, 59, 59);
        return transactionRepo.findByAccountIdAndDateBetween(id, fromDate, toDate);
    }

    return transactionRepo.findByAccountId(id);
}

    @PostMapping
    public Account create(@Valid @RequestBody Account account,
                          @RequestParam Long userId) {
        return service.create(account, userId);
    }

    // 🔐 DEPÓSITO
    @PutMapping("/{id}/deposit")
    public Account deposit(@PathVariable Long id,
                           @RequestParam double amount,
                           @RequestParam Long requesterId) {

        return service.deposit(id, amount, requesterId);
    }

    // 🔐 RETIRADA
    @PutMapping("/{id}/withdraw")
    public Account withdraw(@PathVariable Long id,
                            @RequestParam double amount,
                            @RequestParam Long requesterId) {

        return service.withdraw(id, amount, requesterId);
    }

    // 🔐 TRANSFERENCIA
    @PostMapping("/transfer")
    public void transfer(@RequestParam Long fromId,
                         @RequestParam Long toId,
                         @RequestParam double amount,
                         @RequestParam Long requesterId) {

        service.transfer(fromId, toId, amount, requesterId);
    }

    // 🔐 ELIMINAR CUENTA
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam Long requesterId) {

        service.deleteAccount(id, requesterId);

        return "Cuenta eliminada correctamente";
    }
    
    @PutMapping("/{id}/spending-limit")
    public Account setSpendingLimit(@PathVariable Long id, @RequestParam double limit) {
        return service.setSpendingLimit(id, limit);
    }
}