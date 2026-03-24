package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.deustobank.model.Account;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

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
    public Account getById(Long id) {
    return repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
}
@Transactional
public void transfer(Long fromId, Long toId, double amount) {
    if (fromId.equals(toId)) {
        throw new RuntimeException("No puedes transferir dinero a tu propia cuenta");
    }

    Account from = repo.findById(fromId)
            .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));

    Account to = repo.findById(toId)
            .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));

    if (amount <= 0) {
        throw new RuntimeException("La cantidad debe ser mayor que 0");
    }

    if (from.getBalance() < amount) {
        throw new RuntimeException("Saldo insuficiente");
    }

    double fromBefore = from.getBalance();
    double toBefore = to.getBalance();

    from.setBalance(fromBefore - amount);
    to.setBalance(toBefore + amount);

    repo.save(from);
    repo.save(to);

    Transaction tFrom = new Transaction("TRANSFER_OUT", amount, from);
    tFrom.setBalanceBefore(fromBefore);
    tFrom.setBalanceAfter(from.getBalance());
    transactionRepo.save(tFrom);

    Transaction tTo = new Transaction("TRANSFER_IN", amount, to);
    tTo.setBalanceBefore(toBefore);
    tTo.setBalanceAfter(to.getBalance());
    transactionRepo.save(tTo);
}
}