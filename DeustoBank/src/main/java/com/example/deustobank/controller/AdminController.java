package com.example.deustobank.controller;

import com.example.deustobank.model.User;
import com.example.deustobank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam Long requesterId) {
        User requester = userRepository.findById(requesterId).orElse(null);

        if (requester == null || !"ADMIN".equals(requester.getRole())) {
            return ResponseEntity.status(403).body("Acceso denegado. Se requiere cuenta de Administrador.");
        }

        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/debtors")
    public ResponseEntity<?> getDebtors(@RequestParam Long requesterId) {
        User requester = userRepository.findById(requesterId).orElse(null);

        if (requester == null || !"ADMIN".equals(requester.getRole())) {
            return ResponseEntity.status(403).body("Acceso denegado. Se requiere cuenta de Administrador.");
        }

        List<User> debtors = userRepository.findUsersWithDebt();
        return ResponseEntity.ok(debtors);
    }
}
