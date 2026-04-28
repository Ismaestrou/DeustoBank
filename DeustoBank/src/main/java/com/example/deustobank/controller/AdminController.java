package com.example.deustobank.controller;

import com.example.deustobank.model.User;
import com.example.deustobank.model.SystemStatsDTO;
import com.example.deustobank.repository.UserRepository;
import com.example.deustobank.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.repository.TransactionRepository;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepo;

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

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @RequestParam String newRole, @RequestParam Long requesterId) {

        authService.checkAdmin(requesterId);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!newRole.equals("USER") && !newRole.equals("ADMIN")) {
            return ResponseEntity.badRequest().body("Rol inválido");
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
    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestParam Long requesterId) {

        authService.checkAdmin(requesterId);

        String nuevaPassword = authService.resetPassword(id);

        return ResponseEntity.ok(Map.of("nuevaPassword", nuevaPassword));
    }
}