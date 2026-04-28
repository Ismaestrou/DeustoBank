package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.AccountResponse;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.model.User;
import com.example.deustobank.model.SystemStatsDTO;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;
import com.example.deustobank.service.AlertService;


import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AlertService alertService;

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
        alertService.checkAndAlert(acc, amount);
        return acc;
    }

    // RETIRADA

    public AccountResponse withdraw(Long id, double amount, Long requesterId) {

        Account acc = validarCuentaActiva(id);
        checkAccess(acc, requesterId);

        if (amount <= 0) throw new RuntimeException("Cantidad inválida");

        if (acc.getLimiteGastoMensual() > 0) {
            double nuevoGasto = acc.getGastoMensualActual() + amount;
            if (nuevoGasto > acc.getLimiteGastoMensual()) {
                throw new RuntimeException("Límite de gasto mensual superado");
            }
            acc.setGastoMensualActual(nuevoGasto);
        }

        double before = acc.getBalance();
        double after = before - amount;

        acc.setBalance(after);
        repo.save(acc);
        guardarTransaccion("WITHDRAW", amount, acc, before, after);
        alertService.checkAndAlert(acc, amount);
        return new AccountResponse(acc, comprobarSaldoBajo(acc));
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

        // CONTROL DE ACCESO SOLO SOBRE LA CUENTA ORIGEN
        checkAccess(from, requesterId);

        if (from.getLimiteGastoMensual() > 0) {
            double nuevoGasto = from.getGastoMensualActual() + amount;
            if (nuevoGasto > from.getLimiteGastoMensual()) {
                throw new RuntimeException("No se puede realizar la transferencia, debido a que ha superado el límite mensual");
            }
            from.setGastoMensualActual(nuevoGasto);
        }

        double fromBefore = from.getBalance();
        double toBefore = to.getBalance();

        from.setBalance(fromBefore - amount);
        to.setBalance(toBefore + amount);

        repo.save(from);
        repo.save(to);

        guardarTransaccion("TRANSFER_OUT", amount, from, fromBefore, from.getBalance());
        guardarTransaccion("TRANSFER_IN", amount, to, toBefore, to.getBalance());
        alertService.checkAndAlert(from, amount);
        alertService.checkAndAlert(to, amount);
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
    public SystemStatsDTO getSystemStats() {
        long totalUsers        = userRepo.count();
        long totalTransactions = transactionRepo.count();
        double totalBalance    = repo.sumTotalBalance();
        return new SystemStatsDTO(totalUsers, totalTransactions, totalBalance);
    }

    public Account setLimiteGastoMensual(Long id, double limite) {
        if (limite < 0) throw new RuntimeException("El límite no puede ser negativo");
        Account acc = getById(id);
        acc.setLimiteGastoMensual(limite);
        return repo.save(acc);
    }

    public Account setUmbralSaldoBajo(Long id, double umbral) {
        if (umbral < 0) throw new RuntimeException("El umbral no puede ser negativo");
        Account acc = getById(id);
        acc.setUmbralSaldoBajo(umbral);
        return repo.save(acc);
    }

    private String comprobarSaldoBajo(Account acc) {
        if (acc.getUmbralSaldoBajo() > 0 && acc.getBalance() < acc.getUmbralSaldoBajo()) {
            return "Saldo bajo: tu saldo (" + String.format("%.2f", acc.getBalance()) + " €) está por debajo de tu umbral de alerta (" + String.format("%.2f", acc.getUmbralSaldoBajo()) + " €)";
        }
        return null;
    }
}