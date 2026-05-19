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
 
/**
 * @file AccountService.java
 * @brief Servicio principal para la gestión de cuentas bancarias.
 *
 * Contiene la lógica de negocio relacionada con operaciones bancarias:
 * depósitos, retiradas, transferencias, límites de gasto y umbrales de saldo.
 *
 * @author Equipo DeustoBank
 * @version 1.0
*/

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
 
    /**
     * @brief Obtiene todas las cuentas del sistema.
     * @return Lista de todas las cuentas.
    */

    public List<Account> getAll() {
        return repo.findAll();
    }
 
    /**
     * @brief Obtiene una cuenta por su ID.
     * @param id ID de la cuenta.
     * @return La cuenta encontrada.
     * @throws RuntimeException si la cuenta no existe.
    */

    public Account getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }
 
    /**
     * @brief Obtiene todas las cuentas asociadas a un usuario.
     * @param userId ID del usuario.
     * @return Lista de cuentas del usuario.
    */

    public List<Account> getAccountsByUser(Long userId) {
        return repo.findByUserId(userId);
    }
 
    /**
     * @brief Crea una nueva cuenta bancaria para un usuario.
     * @param account Datos de la cuenta a crear.
     * @param userId ID del usuario propietario.
     * @return La cuenta creada y guardada.
     * @throws RuntimeException si el usuario no existe o el saldo inicial es negativo.
    */

    public Account create(Account account, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
 
        if (account.getBalance() < 0) {
            throw new RuntimeException("El saldo inicial no puede ser negativo");
        }
 
        account.setUser(user);
        return repo.save(account);
    }
 
    /**
     * @brief Realiza un depósito en una cuenta.
     * @param id ID de la cuenta destino.
     * @param amount Cantidad a ingresar (debe ser positiva).
     * @param requesterId ID del usuario que realiza la operación.
     * @return La cuenta actualizada.
     * @throws RuntimeException si la cantidad es inválida o el usuario no tiene acceso.
    */

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
 
    /**
     * @brief Realiza una retirada de fondos de una cuenta.
     *
     * Comprueba el límite de gasto mensual antes de ejecutar la operación.
     *
     * @param id ID de la cuenta.
     * @param amount Cantidad a retirar (debe ser positiva).
     * @param requesterId ID del usuario que realiza la operación.
     * @return AccountResponse con la cuenta actualizada y aviso de saldo bajo si aplica.
     * @throws RuntimeException si se supera el límite mensual o la cantidad es inválida.
    */

    public AccountResponse withdraw(Long id, double amount, Long requesterId) {
        Account acc = validarCuentaActiva(id);
        checkAccess(acc, requesterId);
 
        if (amount <= 0) throw new RuntimeException("Cantidad inválida");
 
        if (acc.getLimiteGastoMensual() > 0) {
            double nuevoGasto = acc.getGastoMensualActual() + amount;
            if (nuevoGasto > acc.getLimiteGastoMensual()) {
                throw new RuntimeException("No se puede realizar la retirada, debido a que ha superado el límite mensual");
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
 
    /**
     * @brief Realiza una transferencia entre dos cuentas.
     *
     * Operación transaccional que descuenta de la cuenta origen y abona en la destino.
     * Comprueba límite de gasto mensual de la cuenta origen.
     *
     * @param fromId ID de la cuenta origen.
     * @param toId ID de la cuenta destino.
     * @param amount Cantidad a transferir.
     * @param requesterId ID del usuario que realiza la operación.
     * @return Mensaje de aviso de saldo bajo si aplica, null en caso contrario.
     * @throws RuntimeException si las cuentas son iguales, la cantidad es inválida o se supera el límite mensual.
    */

    @Transactional
    public String transfer(Long fromId, Long toId, double amount, Long requesterId) {
        if (fromId.equals(toId)) {
            throw new RuntimeException("No puedes transferir a la misma cuenta");
        }
        if (amount <= 0) {
            throw new RuntimeException("Cantidad inválida");
        }
 
        Account from = validarCuentaActiva(fromId);
        Account to = validarCuentaActiva(toId);
 
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
 
        return comprobarSaldoBajo(from);
    }
 
    /**
     * @brief Elimina una cuenta bancaria y su historial de transacciones.
     * @param id ID de la cuenta a eliminar.
     * @param requesterId ID del usuario que solicita la eliminación.
     * @throws RuntimeException si la cuenta tiene saldo negativo o el usuario no tiene acceso.
    */

    public void deleteAccount(Long id, Long requesterId) {
        Account acc = getById(id);
        checkAccess(acc, requesterId);
 
        if (acc.getBalance() < 0) {
            throw new RuntimeException(
                "No puedes eliminar la cuenta: saldo negativo (" + acc.getBalance() + ")");
        }
 
        List<Transaction> transactions = transactionRepo.findByAccountIdOrderByDateDesc(id);
        if (!transactions.isEmpty()) {
            transactionRepo.deleteAll(transactions);
        }
 
        repo.delete(acc);
    }
 
    /**
     * @brief Establece el límite de gasto mensual de una cuenta.
     * @param id ID de la cuenta.
     * @param limite Límite mensual en euros (debe ser >= 0).
     * @return La cuenta actualizada.
     * @throws RuntimeException si el límite es negativo.
    */

    public Account setLimiteGastoMensual(Long id, double limite) {
        if (limite < 0) throw new RuntimeException("El límite no puede ser negativo");
        Account acc = getById(id);
        acc.setLimiteGastoMensual(limite);
        return repo.save(acc);
    }
 
    /**
     * @brief Establece el umbral de aviso de saldo bajo de una cuenta.
     * @param id ID de la cuenta.
     * @param umbral Umbral en euros (debe ser >= 0).
     * @return La cuenta actualizada.
     * @throws RuntimeException si el umbral es negativo.
    */

    public Account setUmbralSaldoBajo(Long id, double umbral) {
        if (umbral < 0) throw new RuntimeException("El umbral no puede ser negativo");
        Account acc = getById(id);
        acc.setUmbralSaldoBajo(umbral);
        return repo.save(acc);
    }
 
    /**
     * @brief Calcula el saldo total de todas las cuentas de un usuario.
     * @param userId ID del usuario.
     * @return Suma de saldos de todas sus cuentas.
    */

    public double getTotalBalanceByUser(Long userId) {
        List<Account> accounts = repo.findByUserId(userId);
        return accounts.stream().mapToDouble(Account::getBalance).sum();
    }
 
    /**
     * @brief Obtiene estadísticas globales del sistema.
     * @return DTO con número total de usuarios, transacciones y saldo acumulado.
    */

    public SystemStatsDTO getSystemStats() {
        long totalUsers        = userRepo.count();
        long totalTransactions = transactionRepo.count();
        double totalBalance    = repo.sumTotalBalance();
        return new SystemStatsDTO(totalUsers, totalTransactions, totalBalance);
    }
 
    /**
     * @brief Valida que la cuenta existe y que su usuario está activo.
     * @param id ID de la cuenta.
     * @return La cuenta validada.
     * @throws RuntimeException si el usuario está bloqueado.
    */

    private Account validarCuentaActiva(Long id) {
        Account acc = getById(id);
        if (!acc.getUser().isActive()) {
            throw new RuntimeException("Operación denegada: El usuario está bloqueado.");
        }
        return acc;
    }
 
    /**
     * @brief Guarda una transacción en el historial.
     * @param tipo Tipo de transacción (DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT).
     * @param amount Cantidad de la operación.
     * @param acc Cuenta afectada.
     * @param before Saldo antes de la operación.
     * @param after Saldo después de la operación.
    */

    private void guardarTransaccion(String tipo, double amount, Account acc,
                                    double before, double after) {
        Transaction t = new Transaction(tipo, amount, acc);
        t.setBalanceBefore(before);
        t.setBalanceAfter(after);
        transactionRepo.save(t);
    }
 
    /**
     * @brief Verifica que el usuario solicitante tiene acceso a la cuenta.
     *
     * Los administradores tienen acceso a cualquier cuenta.
     * Los usuarios normales solo pueden acceder a sus propias cuentas.
     *
     * @param acc Cuenta a la que se intenta acceder.
     * @param requesterId ID del usuario solicitante.
     * @throws RuntimeException si el usuario no tiene permisos.
    */

    private void checkAccess(Account acc, Long requesterId) {
        User requester = userRepo.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
 
        if (!"ADMIN".equals(requester.getRole()) &&
            !acc.getUser().getId().equals(requesterId)) {
            throw new RuntimeException("No autorizado");
        }
    }
 
    /**
     * @brief Comprueba si el saldo de la cuenta está por debajo del umbral configurado.
     * @param acc Cuenta a comprobar.
     * @return Mensaje de aviso si el saldo es bajo, null si no aplica.
    */

    private String comprobarSaldoBajo(Account acc) {
        if (acc.getUmbralSaldoBajo() > 0 && acc.getBalance() < acc.getUmbralSaldoBajo()) {
            return "Saldo bajo: tu saldo (" + String.format("%.2f", acc.getBalance())
                + " €) está por debajo de tu umbral de alerta ("
                + String.format("%.2f", acc.getUmbralSaldoBajo()) + " €)";
        }
        return null;
    }
}