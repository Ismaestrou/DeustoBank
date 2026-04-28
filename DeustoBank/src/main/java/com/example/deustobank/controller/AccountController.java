package com.example.deustobank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.AccountResponse;
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

    @GetMapping
    public List<Account> getAll() {
        return service.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<Account> getByUser(@PathVariable Long userId) {
        return service.getAccountsByUser(userId);
    }

    @GetMapping("/{id}")
    public Account getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> getTransactions(@PathVariable Long id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        if (from != null && to != null) {
            LocalDateTime fromDate = LocalDate.parse(from).atStartOfDay();
            LocalDateTime toDate = LocalDate.parse(to).atTime(23, 59, 59);
            return transactionRepo.findByAccountIdAndDateBetween(id, fromDate, toDate);
        }
        return transactionRepo.findByAccountId(id);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        return transactionRepo.findByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Account create(@Valid @RequestBody Account account,
                          @RequestParam Long userId) {
        return service.create(account, userId);
    }

    @PutMapping("/{id}/deposit")
    public Account deposit(@PathVariable Long id,
                           @RequestParam double amount,
                           @RequestParam Long requesterId) {
        return service.deposit(id, amount, requesterId);
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable Long id,
                        @RequestParam double amount,
                        @RequestParam Long requesterId) {
        try {
            return ResponseEntity.ok(service.withdraw(id, amount, requesterId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestParam Long fromId,
                                  @RequestParam Long toId,
                                  @RequestParam double amount,
                                  @RequestParam Long requesterId) {
        try {
            String alerta = service.transfer(fromId, toId, amount, requesterId);
            if (alerta != null) {
                return ResponseEntity.ok(alerta);
            }
            return ResponseEntity.ok("Transferencia realizada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam Long requesterId) {
        service.deleteAccount(id, requesterId);
        return "Cuenta eliminada correctamente";
    }

    @PutMapping("/{id}/limite-gasto-mensual")
    public Account setLimiteGastoMensual(@PathVariable Long id, @RequestParam double limite) {
        return service.setLimiteGastoMensual(id, limite);
    }

    @PutMapping("/{id}/umbral-saldo-bajo")
    public Account setUmbralSaldoBajo(@PathVariable Long id, @RequestParam double umbral) {
        return service.setUmbralSaldoBajo(id, umbral);
    }
}