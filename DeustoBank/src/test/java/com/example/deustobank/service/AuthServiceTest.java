package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private AccountRepository accountRepo;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setDni("12345678A");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setRole("USER");
        testUser.setActive(true);
        testUser.setFailedLoginAttempts(0);
    }

    @Test
    void register_Success() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepo.findByDni(anyString())).thenReturn(Optional.empty());
        when(userRepo.saveAndFlush(any(User.class))).thenReturn(testUser);
        when(accountRepo.save(any(Account.class))).thenReturn(new Account());

        User result = authService.register(testUser);

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepo).saveAndFlush(any(User.class));
        verify(accountRepo).save(any(Account.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.register(testUser));
        assertEquals("El email ya está en uso", exception.getMessage());
    }

    @Test
    void register_DniAlreadyExists() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepo.findByDni(anyString())).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.register(testUser));
        assertEquals("El DNI ya está registrado", exception.getMessage());
    }

    @Test
    void register_ExceptionCreatingAccount() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepo.findByDni(anyString())).thenReturn(Optional.empty());
        when(userRepo.saveAndFlush(any(User.class))).thenReturn(testUser);
        when(accountRepo.save(any(Account.class))).thenThrow(new RuntimeException("DB Error"));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.register(testUser));
        assertEquals("Error creando cuenta", exception.getMessage());
    }

    @Test
    void login_Success() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        testUser.setPassword(encoder.encode("password123"));

        when(userRepo.findByDni("12345678A")).thenReturn(Optional.of(testUser));

        User result = authService.login("12345678A", "password123");

        assertNotNull(result);
        assertEquals(0, result.getFailedLoginAttempts());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void login_UserNotFound() {
        when(userRepo.findByDni(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> authService.login("12345678A", "password123"));
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void login_AccountBlocked() {
        testUser.setActive(false);
        when(userRepo.findByDni("12345678A")).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.login("12345678A", "password123"));
        assertEquals("CUENTA_BLOQUEADA", exception.getMessage());
    }

    @Test
    void login_WrongPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        testUser.setPassword(encoder.encode("password123"));
        testUser.setFailedLoginAttempts(1);

        when(userRepo.findByDni("12345678A")).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.login("12345678A", "wrongpassword"));
        assertTrue(exception.getMessage().contains("Contraseña incorrecta"));
        assertEquals(2, testUser.getFailedLoginAttempts());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void login_WrongPassword_BlocksAccount() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        testUser.setPassword(encoder.encode("password123"));
        testUser.setFailedLoginAttempts(2);

        when(userRepo.findByDni("12345678A")).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.login("12345678A", "wrongpassword"));
        assertEquals("CUENTA_BLOQUEADA", exception.getMessage());
        assertEquals(3, testUser.getFailedLoginAttempts());
        assertFalse(testUser.isActive());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void checkAdmin_Success() {
        testUser.setRole("ADMIN");
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> authService.checkAdmin(1L));
    }

    @Test
    void checkAdmin_NotAdmin() {
        testUser.setRole("USER");
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.checkAdmin(1L));
        assertEquals("Acceso denegado: solo administradores", exception.getMessage());
    }

    @Test
    void resetPassword_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        String newPassword = authService.resetPassword(1L);

        assertEquals("Deusto1!", newPassword);
        verify(userRepo).save(testUser);
    }

    @Test
    void updateProfile_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        User updated = authService.updateProfile(1L, "new@test.com", "987654321");

        assertEquals("new@test.com", testUser.getEmail());
        assertEquals("987654321", testUser.getPhone());
    }

    @Test
    void updateProfile_EmailInUse() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        User otherUser = new User();
        otherUser.setId(2L);
        when(userRepo.findByEmail("inuse@test.com")).thenReturn(Optional.of(otherUser));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.updateProfile(1L, "inuse@test.com", null));
        assertEquals("El email ya está en uso por otro usuario", exception.getMessage());
    }
}
