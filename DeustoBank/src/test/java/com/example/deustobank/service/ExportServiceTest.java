package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deustobank.model.User;
import com.example.deustobank.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExportService exportService;

    @Test
    void exportUsersAsCsv_ReturnsValidCsv() throws IOException {
        User u1 = new User();
        u1.setId(1L);
        u1.setFullName("Juan Perez");
        u1.setDni("11111111A");
        u1.setEmail("juan@test.com");
        u1.setPhone("123456789");
        u1.setRole("USER");
        u1.setActive(true);
        u1.setFailedLoginAttempts(0);

        when(userRepository.findAll()).thenReturn(List.of(u1));

        byte[] csv = exportService.exportUsersAsCsv();
        
        assertNotNull(csv);
        String csvString = new String(csv, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csvString.contains("ID,Nombre Completo,DNI,Email,Teléfono,Rol,Estado,Intentos Fallidos"));
        assertTrue(csvString.contains("Juan Perez"));
        assertTrue(csvString.contains("11111111A"));
    }

    @Test
    void exportUsersAsCsv_WithNullFields() throws IOException {
        User u1 = new User();
        u1.setId(2L);
        // leaving fields null to test the safe() method

        when(userRepository.findAll()).thenReturn(List.of(u1));

        byte[] csv = exportService.exportUsersAsCsv();
        
        assertNotNull(csv);
        String csvString = new String(csv, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csvString.contains("2,\"\",\"\",\"\",\"\""));
    }
}
