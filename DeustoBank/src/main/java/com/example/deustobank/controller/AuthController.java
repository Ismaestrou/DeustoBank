package com.example.deustobank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.deustobank.model.User;
import com.example.deustobank.service.AuthService;
import com.example.deustobank.service.TokenBlacklistService;
import com.example.deustobank.repository.UserRepository;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenBlacklistService tokenBlacklist;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User savedUser = service.register(user);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String dni,
            @RequestParam String password) {
        try {
            User user = service.login(dni, password);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {
        try {
            User updated = service.updateProfile(id, email, phone);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam(required = false) String sessionToken) {
        if (sessionToken != null && !sessionToken.isBlank()) {
            tokenBlacklist.invalidate(sessionToken);
        }
        return ResponseEntity.ok(java.util.Map.of("message", "Sesión cerrada correctamente"));
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable Long id,
            @RequestParam String passwordActual,
            @RequestParam String passwordNueva) {
        try {
            service.changePassword(id, passwordActual, passwordNueva);
            return ResponseEntity.ok("Contraseña cambiada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}