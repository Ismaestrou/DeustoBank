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

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

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
}