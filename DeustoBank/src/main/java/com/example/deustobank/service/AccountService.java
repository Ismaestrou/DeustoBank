package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private UserRepository userRepo;

    // OBTENER CUENTAS

    public List<Account> getAll() {
        return repo.findAll();
    }

    public Account getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }

    public List<Account> getAccountsByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    // CREAR CUENTA

    public Account create(Account account, Long userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (account.getBalance() < 0) {
            throw new RuntimeException("El saldo inicial no puede ser negativo");
        }

        account.setUser(user);

        return repo.save(account);
    }

    // DEPÓSITO

    public Account deposit(Long id, double amount, Long requesterId) {

        Account acc = validarCuentaActiva(id);

        checkAccess(acc, requesterId);

        if (amount <= 0) {
            throw new RuntimeException("Cantidad inválida");
        }

        double before = acc.getBalance();
        double after = before + amount;

        acc.setBalance(after);
        repo.save(acc);

        guardarTransaccion("DEPOSIT", amount, acc, before, after);

        return acc;
    }

    // RETIRADA

    public Account withdraw(Long id, double amount, Long requesterId) {

        Account acc = validarCuentaActiva(id);
    
        checkAccess(acc, requesterId);
    
        if (amount <= 0) {
            throw new RuntimeException("Cantidad inválida");
        }
    
        if (acc.getMonthlySpendingLimit() > 0) {
            double newSpending = acc.getCurrentMonthSpending() + amount;
            if (newSpending > acc.getMonthlySpendingLimit()) {
                throw new RuntimeException("Límite de gasto mensual superado");
            }
            acc.setCurrentMonthSpending(newSpending);
        }
    
        double before = acc.getBalance();
        double after = before - amount;
    
        acc.setBalance(after);
        repo.save(acc);
    
        guardarTransaccion("WITHDRAW", amount, acc, before, after);
    
        return acc;
    }

    // TRANSFERENCIA

    @Transactional
    public void transfer(Long fromId, Long toId, double amount, Long requesterId) {

        if (fromId.equals(toId)) {
            throw new RuntimeException("No puedes transferir a la misma cuenta");
        }

        if (amount <= 0) {
            throw new RuntimeException("Cantidad inválida");
        }

        Account from = validarCuentaActiva(fromId);
        Account to = validarCuentaActiva(toId);

        // 🔥 CONTROL DE ACCESO SOLO SOBRE LA CUENTA ORIGEN
        checkAccess(from, requesterId);

        if (from.getMonthlySpendingLimit() > 0) {
            double newSpending = from.getCurrentMonthSpending() + amount;
            if (newSpending > from.getMonthlySpendingLimit()) {
                throw new RuntimeException("Límite de gasto mensual superado en la cuenta origen");
            }
            from.setCurrentMonthSpending(newSpending);
        }

        double fromBefore = from.getBalance();
        double toBefore = to.getBalance();

        from.setBalance(fromBefore - amount);
        to.setBalance(toBefore + amount);

        repo.save(from);
        repo.save(to);

        guardarTransaccion("TRANSFER_OUT", amount, from, fromBefore, from.getBalance());
        guardarTransaccion("TRANSFER_IN", amount, to, toBefore, to.getBalance());
    }


    // ELIMINAR CUENTA

    public void deleteAccount(Long id, Long requesterId) {

        Account acc = getById(id);

        checkAccess(acc, requesterId);

        if (acc.getBalance() < 0) {
            throw new RuntimeException(
                "No puedes eliminar la cuenta: saldo negativo (" + acc.getBalance() + ")"
            );
        }

        List<Transaction> transactions = transactionRepo.findByAccountId(id);

        if (!transactions.isEmpty()) {
            transactionRepo.deleteAll(transactions);
        }

        repo.delete(acc);
    }


    private Account validarCuentaActiva(Long id) {
        Account acc = getById(id);

        if (!acc.getUser().isActive()) {
            throw new RuntimeException("Operación denegada: El usuario está bloqueado.");
        }

        return acc;
    }

    private void guardarTransaccion(String tipo, double amount, Account acc,
                                    double before, double after) {

        Transaction t = new Transaction(tipo, amount, acc);
        t.setBalanceBefore(before);
        t.setBalanceAfter(after);

        transactionRepo.save(t);
    }

    private void checkAccess(Account acc, Long requesterId) {
        User requester = userRepo.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es admin y no es dueño → denegar
        if (!"ADMIN".equals(requester.getRole()) &&
            !acc.getUser().getId().equals(requesterId)) {
            throw new RuntimeException("No autorizado");
        }
    }

    public double getTotalBalanceByUser(Long userId) {
        List<Account> accounts = repo.findByUserId(userId);

        return accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    public Account setSpendingLimit(Long id, double limit) {
        if (limit < 0) throw new RuntimeException("El límite no puede ser negativo");
        Account acc = getById(id);
        acc.setMonthlySpendingLimit(limit);
        return repo.save(acc);
    }
}