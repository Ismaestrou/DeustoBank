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
 
<<<<<<< HEAD
    @Autowired
    private PdfService pdfService;

    // Obtiene todas las cuentas del sistema
=======
    /**
     * @brief GET /accounts — Obtiene todas las cuentas del sistema.
     * @return Lista de todas las cuentas.
    */

>>>>>>> parent of eb770fd (Resolve merge conflicts from stash)
    @GetMapping
    public List<Account> getAll() {
        return service.getAll();
    }
 
    // Obtiene las cuentas de un usuario específico
    @GetMapping("/user/{userId}")
    public List<Account> getByUser(@PathVariable Long userId) {
        return service.getAccountsByUser(userId);
    }
 
    // Obtiene detalles de una cuenta por ID
    @GetMapping("/{id}")
    public Account getById(@PathVariable Long id) {
        return service.getById(id);
    }
 
    // Obtiene historial de transacciones con opción de filtro de fechas
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
 
    // Obtiene detalle de una transacción específica por ID
    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        return transactionRepo.findByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    // Crea una nueva cuenta bancaria
    @PostMapping
    public Account create(@Valid @RequestBody Account account,
                          @RequestParam Long userId) {
        return service.create(account, userId);
    }
 
    // Realiza un depósito en una cuenta
    @PutMapping("/{id}/deposit")
    public ResponseEntity<?> deposit(@PathVariable Long id,
                                    @RequestParam double amount,
                                    @RequestParam Long requesterId) {
        try {
            return ResponseEntity.ok(service.deposit(id, amount, requesterId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error deposit");
        }
    }
 
    // Realiza una retirada de una cuenta
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
 
    // Realiza una transferencia entre cuentas
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
 
    // Elimina una cuenta bancaria y su historial
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam Long requesterId) {
        service.deleteAccount(id, requesterId);
        return "Cuenta eliminada correctamente";
    }
 
    // Configura el límite de gasto mensual
    @PutMapping("/{id}/limite")
    public Account setLimiteGastoMensual(@PathVariable Long id, @RequestParam double limite) {
        return service.setLimiteGastoMensual(id, limite);
    }
 
    // Configura el umbral de saldo bajo
    @PutMapping("/{id}/umbral")
    public Account setUmbralSaldoBajo(@PathVariable Long id, @RequestParam double umbral) {
        return service.setUmbralSaldoBajo(id, umbral);
    }
<<<<<<< HEAD

    // Genera y descarga el extracto bancario en PDF
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
=======
>>>>>>> parent of eb770fd (Resolve merge conflicts from stash)
}