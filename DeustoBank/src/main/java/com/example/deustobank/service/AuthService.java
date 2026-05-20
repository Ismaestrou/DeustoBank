package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.deustobank.model.User;
import com.example.deustobank.model.Account;
import com.example.deustobank.repository.UserRepository;
import com.example.deustobank.repository.AccountRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AccountRepository accountRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User register(User user) {

        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está en uso");
        }

        if (userRepo.findByDni(user.getDni()).isPresent()) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        user.setPassword(encoder.encode(user.getPassword()));

        User savedUser = userRepo.saveAndFlush(user);

        try {
            Account account = new Account();
            account.setOwnerName(savedUser.getFullName());
            account.setBalance(0);
            account.setUser(savedUser);

            accountRepo.save(account);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creando cuenta");
        }

        return savedUser;
    }

    public User login(String dni, String password) {

        User user = userRepo.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.isActive()) {
            throw new RuntimeException("CUENTA_BLOQUEADA");
        }

        if (!encoder.matches(password, user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= 3) {
                user.setActive(false);
                userRepo.save(user);
                throw new RuntimeException("CUENTA_BLOQUEADA");
            }

            userRepo.save(user);
            throw new RuntimeException("Contraseña incorrecta. Intentos restantes: " + (3 - user.getFailedLoginAttempts()));
        }

        user.setFailedLoginAttempts(0);
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepo.save(user);

        return user;
    }

    public User getUserOrThrow(Long id) {
        return userRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public void checkAdmin(Long userId) {
        User user = getUserOrThrow(userId);

        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Acceso denegado: solo administradores");
        }
    }

    public String resetPassword(Long userId) {
        User user = getUserOrThrow(userId);

        String nuevaPassword = "Deusto" + userId + "!";
        user.setPassword(encoder.encode(nuevaPassword));
        userRepo.save(user);

        return nuevaPassword;
    }

    public User updateProfile(Long id, String email, String phone) {
        User user = getUserOrThrow(id);

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepo.findByEmail(email).isPresent()) {
                throw new RuntimeException("El email ya está en uso por otro usuario");
            }
            user.setEmail(email);
        }

        if (phone != null) {
            user.setPhone(phone);
        }

        return userRepo.save(user);
    }

    public void changePassword(Long id, String passwordActual, String passwordNueva) {
        User user = getUserOrThrow(id);

        if (!encoder.matches(passwordActual, user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        if (passwordNueva == null || passwordNueva.length() < 4) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 4 caracteres");
        }

        user.setPassword(encoder.encode(passwordNueva));
        userRepo.save(user);
    }
}