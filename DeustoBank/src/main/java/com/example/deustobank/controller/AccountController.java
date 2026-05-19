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
import com.example.deustobank.service.PdfService;
import com.example.deustobank.repository.TransactionRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * @file AccountController.java
 * @brief Controlador REST para la gestión de cuentas bancarias.
 *
 * Expone los endpoints de la API relacionados con cuentas:
 * creación, consulta, depósitos, retiradas, transferencias y eliminación.
 *
 * Base URL: /accounts
 *
 * @author Equipo DeustoBank
 * @version 1.0
*/

@RestController
@RequestMapping("/accounts")
@CrossOrigin
public class AccountController {
 
    @Autowired
    private AccountService service;
 
    @Autowired
    private TransactionRepository transactionRepo;
 
    /**
     * @brief GET /accounts — Obtiene todas las cuentas del sistema.
     * @return Lista de todas las cuentas.
    */

    @Autowired
    private PdfService pdfService;

    @GetMapping
    public List<Account> getAll() {
        return service.getAll();
    }
 
    /**
     * @brief GET /accounts/user/{userId} — Obtiene las cuentas de un usuario.
     * @param userId ID del usuario.
     * @return Lista de cuentas del usuario.
    */

    @GetMapping("/user/{userId}")
    public List<Account> getByUser(@PathVariable Long userId) {
        return service.getAccountsByUser(userId);
    }
 
    /**
     * @brief GET /accounts/{id} — Obtiene una cuenta por su ID.
     * @param id ID de la cuenta.
     * @return La cuenta encontrada.
    */

    @GetMapping("/{id}")
    public Account getById(@PathVariable Long id) {
        return service.getById(id);
    }
 
    /**
     * @brief GET /accounts/{id}/transactions — Obtiene el historial de transacciones de una cuenta.
     *
     * Soporta filtrado por rango de fechas mediante los parámetros opcionales {@code from} y {@code to}.
     *
     * @param id ID de la cuenta.
     * @param requesterId ID del usuario que solicita el historial (debe ser el propietario).
     * @param from Fecha inicio del filtro (formato: yyyy-MM-dd), opcional.
     * @param to Fecha fin del filtro (formato: yyyy-MM-dd), opcional.
     * @return Lista de transacciones o 403 si no tiene permisos.
    */

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable Long id,
            @RequestParam Long requesterId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Account acc = service.getById(id);
        if (!acc.getUser().getId().equals(requesterId)) {
            return ResponseEntity.status(403).body("No autorizado");
        }
        if (from != null && to != null) {
            LocalDateTime fromDate = LocalDate.parse(from).atStartOfDay();
            LocalDateTime toDate = LocalDate.parse(to).atTime(23, 59, 59);
            return ResponseEntity.ok(transactionRepo.findByAccountIdAndDateBetween(id, fromDate, toDate));
        }
        return ResponseEntity.ok(transactionRepo.findByAccountIdOrderByDateDesc(id));
    }
 
    /**
     * @brief GET /accounts/transactions/{id} — Obtiene el detalle de una transacción.
     * @param id ID de la transacción.
     * @return La transacción encontrada o 404 si no existe.
    */

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        return transactionRepo.findByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    /**
     * @brief POST /accounts — Crea una nueva cuenta bancaria.
     * @param account Datos de la cuenta (body JSON).
     * @param userId ID del usuario propietario.
     * @return La cuenta creada.
    */

    @PostMapping
    public Account create(@Valid @RequestBody Account account,
                          @RequestParam Long userId) {
        return service.create(account, userId);
    }
 
    /**
     * @brief PUT /accounts/{id}/deposit — Realiza un ingreso en una cuenta.
     * @param id ID de la cuenta.
     * @param amount Cantidad a ingresar.
     * @param requesterId ID del usuario que realiza el ingreso.
     * @return La cuenta actualizada o 400 si hay error.
    */

    @PutMapping("/{id}/deposit")
    public ResponseEntity<?> deposit(@PathVariable Long id,
                                    @RequestParam double amount,
                                    @RequestParam Long requesterId,
                                    @RequestParam(required = false) String concepto) {
        try {
            return ResponseEntity.ok(service.deposit(id, amount, requesterId, concepto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error deposit");
        }
    }
 
    /**
     * @brief PUT /accounts/{id}/withdraw — Realiza una retirada de fondos.
     * @param id ID de la cuenta.
     * @param amount Cantidad a retirar.
     * @param requesterId ID del usuario que realiza la retirada.
     * @return La cuenta actualizada o 400 con mensaje de error.
    */

    @PutMapping("/{id}/withdraw")
        public ResponseEntity<?> withdraw(@PathVariable Long id,
                            @RequestParam double amount,
                            @RequestParam Long requesterId,
                            @RequestParam(required = false) String concepto) {
            try {
                return ResponseEntity.ok(service.withdraw(id, amount, requesterId, concepto));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
    /**
     * @brief POST /accounts/transfer — Realiza una transferencia entre cuentas.
     * @param fromId ID de la cuenta origen.
     * @param toId ID de la cuenta destino.
     * @param amount Cantidad a transferir.
     * @param requesterId ID del usuario que realiza la transferencia.
     * @return Confirmación de la operación, con alerta de saldo bajo si aplica.
    */

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestParam Long fromId,
                                    @RequestParam Long toId,
                                    @RequestParam double amount,
                                    @RequestParam Long requesterId) {
        try {
            String alerta = service.transfer(fromId, toId, amount, requesterId);
            if (alerta != null) {
                return ResponseEntity.ok(
                    java.util.Map.of("message", "Transferencia realizada", "alert", alerta));
            }
            return ResponseEntity.ok("Transferencia realizada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
 
    /**
     * @brief DELETE /accounts/{id} — Elimina una cuenta bancaria.
     * @param id ID de la cuenta a eliminar.
     * @param requesterId ID del usuario que solicita la eliminación.
     * @return Mensaje de confirmación.
    */

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam Long requesterId) {
        service.deleteAccount(id, requesterId);
        return "Cuenta eliminada correctamente";
    }
 
    /**
     * @brief PUT /accounts/{id}/limite — Establece el límite de gasto mensual.
     * @param id ID de la cuenta.
     * @param limite Límite en euros.
     * @return La cuenta actualizada.
    */

    @PutMapping("/{id}/limite")
    public Account setLimiteGastoMensual(@PathVariable Long id, @RequestParam double limite) {
        return service.setLimiteGastoMensual(id, limite);
    }
 
    /**
     * @brief PUT /accounts/{id}/umbral — Establece el umbral de aviso de saldo bajo.
     * @param id ID de la cuenta.
     * @param umbral Umbral en euros.
     * @return La cuenta actualizada.
    */

    @PutMapping("/{id}/umbral")
    public Account setUmbralSaldoBajo(@PathVariable Long id, @RequestParam double umbral) {
        return service.setUmbralSaldoBajo(id, umbral);
    }

    @GetMapping("/{id}/statement/pdf")
    public ResponseEntity<byte[]> getPdfStatement(@PathVariable Long id, @RequestParam Long requesterId) {
        Account acc = service.getById(id);
        if (!acc.getUser().getId().equals(requesterId)) {
            return ResponseEntity.status(403).build();
        }
        
        List<Transaction> transactions = transactionRepo.findByAccountIdOrderByDateDesc(id);
        byte[] pdfBytes = pdfService.generateStatement(acc, transactions);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "extracto_cuenta_" + id + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}