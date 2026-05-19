package com.example.deustobank.controller;

import com.example.deustobank.model.User;
import com.example.deustobank.model.SystemStatsDTO;
import com.example.deustobank.repository.UserRepository;
import com.example.deustobank.service.AuthService;
import com.example.deustobank.service.ExportService;
import com.example.deustobank.model.SuspiciousAlert;
import com.example.deustobank.repository.SuspiciousAlertRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.repository.TransactionRepository;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private SuspiciousAlertRepository alertRepo;

    @Autowired
    private ExportService exportService;

    @Autowired
    private com.example.deustobank.service.UserService userService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/debtors")
    public ResponseEntity<?> getDebtors(@RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        List<User> debtors = userRepository.findUsersWithDebt();
        return ResponseEntity.ok(debtors);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, @RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        User targetUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        targetUser.setActive(!targetUser.isActive());
        userRepository.save(targetUser);
        return ResponseEntity.ok(targetUser);
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRolePatch(@PathVariable Long id,
            @RequestParam String newRole, @RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        if (id.equals(requesterId)) {
            return ResponseEntity.badRequest().body("No puedes cambiar tu propio rol");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!newRole.equals("USER") && !newRole.equals("ADMIN")) {
            return ResponseEntity.badRequest().body("Rol inválido. Valores permitidos: USER, ADMIN");
        }
        user.setRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/users/{id}/summary")
    public ResponseEntity<?> getUserSummary(@PathVariable Long id, @RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        double totalBalance = accountService.getTotalBalanceByUser(id);
        long transactions = transactionRepo.countByAccountUserId(id);
        return ResponseEntity.ok(Map.of(
            "totalBalance", totalBalance,
            "transactions", transactions
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats(@RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        SystemStatsDTO stats = accountService.getSystemStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(@RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        return ResponseEntity.ok(alertRepo.findAllByOrderByCreatedAtDesc());
    }

    @PutMapping("/alerts/{id}/review")
    public ResponseEntity<?> markAsReviewed(@PathVariable Long id, @RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        SuspiciousAlert alert = alertRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alert.setReviewed(true);
        alertRepo.save(alert);
        return ResponseEntity.ok(Map.of("message", "Alerta marcada como revisada"));
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        String nuevaPassword = authService.resetPassword(id);
        return ResponseEntity.ok(Map.of("nuevaPassword", nuevaPassword));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestParam Long requesterId,
                                        @RequestBody Map<String, String> datos) {
        authService.checkAdmin(requesterId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
 
        if (datos.containsKey("fullName") && !datos.get("fullName").isBlank())
            user.setFullName(datos.get("fullName"));
        if (datos.containsKey("email") && !datos.get("email").isBlank())
            user.setEmail(datos.get("email"));
        if (datos.containsKey("phone"))
            user.setPhone(datos.get("phone"));
        if (datos.containsKey("dni") && !datos.get("dni").isBlank())
            user.setDni(datos.get("dni"));
 
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
 
    @GetMapping("/users/export/csv")
    public ResponseEntity<byte[]> exportUsersCsv(@RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        try {
            byte[] csvBytes = exportService.exportUsersAsCsv();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "usuarios_deustobank.csv");
            headers.setContentLength(csvBytes.length);
            return ResponseEntity.ok().headers(headers).body(csvBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestParam Long requesterId) {
        authService.checkAdmin(requesterId);
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}