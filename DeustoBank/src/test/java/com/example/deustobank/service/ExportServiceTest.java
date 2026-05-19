package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    void exportUsersAsCsv_ReturnsValidCsv_ActiveUser() throws IOException {
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
        String csvString = new String(csv, StandardCharsets.UTF_8);

        assertNotNull(csv);
        assertTrue(csvString.contains("ID,Nombre Completo,DNI,Email,Teléfono,Rol,Estado,Intentos Fallidos"));
        assertTrue(csvString.contains("Juan Perez"));
        assertTrue(csvString.contains("11111111A"));
        assertTrue(csvString.contains("ACTIVO"));
    }

    @Test
    void exportUsersAsCsv_BlockedUser_ShowsBloqueado() throws IOException {
        User u = new User();
        u.setId(2L);
        u.setFullName("Ana Gomez");
        u.setDni("22222222B");
        u.setEmail("ana@test.com");
        u.setPhone("987654321");
        u.setRole("USER");
        u.setActive(false); // blocked
        u.setFailedLoginAttempts(3);

        when(userRepository.findAll()).thenReturn(List.of(u));

        byte[] csv = exportService.exportUsersAsCsv();
        String csvString = new String(csv, StandardCharsets.UTF_8);

        assertTrue(csvString.contains("BLOQUEADO"));
        assertTrue(csvString.contains("Ana Gomez"));
    }

    @Test
    void exportUsersAsCsv_WithNullFields_UsesEmptyString() throws IOException {
        User u = new User();
        u.setId(3L);
        // all string fields null

        when(userRepository.findAll()).thenReturn(List.of(u));

        byte[] csv = exportService.exportUsersAsCsv();
        String csvString = new String(csv, StandardCharsets.UTF_8);

        assertNotNull(csv);
        // safe() should return empty strings for nulls
        assertTrue(csvString.contains("3,\"\",\"\",\"\",\"\""));
    }

    @Test
    void exportUsersAsCsv_WithQuotesInName_EscapesQuotes() throws IOException {
        User u = new User();
        u.setId(4L);
        u.setFullName("O\"Brien");  // name with quote
        u.setDni("44444444D");
        u.setEmail("obrien@test.com");
        u.setPhone("111222333");
        u.setRole("USER");
        u.setActive(true);
        u.setFailedLoginAttempts(0);

        when(userRepository.findAll()).thenReturn(List.of(u));

        byte[] csv = exportService.exportUsersAsCsv();
        String csvString = new String(csv, StandardCharsets.UTF_8);

        // quote in name should be escaped as ""
        assertTrue(csvString.contains("O\"\"Brien"));
    }

    @Test
    void exportUsersAsCsv_EmptyUserList_OnlyHeaderRow() throws IOException {
        when(userRepository.findAll()).thenReturn(List.of());

        byte[] csv = exportService.exportUsersAsCsv();
        String csvString = new String(csv, StandardCharsets.UTF_8);

        assertTrue(csvString.contains("ID,Nombre Completo"));
        // only the header line — no data rows
        long lineCount = csvString.lines().filter(l -> !l.isBlank()).count();
        assertEquals(1, lineCount);
    }

    @Test
    void exportUsersAsCsv_StartsWithUtf8BomBytes() throws IOException {
        when(userRepository.findAll()).thenReturn(List.of());

        byte[] csv = exportService.exportUsersAsCsv();

        // UTF-8 BOM: EF BB BF
        assertTrue(csv.length >= 3);
        assertEquals((byte) 0xEF, csv[0]);
        assertEquals((byte) 0xBB, csv[1]);
        assertEquals((byte) 0xBF, csv[2]);
    }
}