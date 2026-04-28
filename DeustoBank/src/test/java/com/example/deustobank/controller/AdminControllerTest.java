package com.example.deustobank.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.deustobank.model.SuspiciousAlert;
import com.example.deustobank.model.SystemStatsDTO;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.SuspiciousAlertRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.service.AuthService;
import com.example.deustobank.service.ExportService;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthService authService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionRepository transactionRepo;

    @MockBean
    private SuspiciousAlertRepository alertRepo;

    @MockBean
    private ExportService exportService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(2L);
        testUser.setActive(true);
        testUser.setRole("USER");
    }

    @Test
    void getAllUsers_Success() throws Exception {
        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/admin/users")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void toggleUserStatus_Success() throws Exception {
        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/admin/users/2/status")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false)); // Toggled from true to false
    }

    @Test
    void changeUserRolePatch_Success() throws Exception {
        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(patch("/api/admin/users/2/role")
                .param("newRole", "ADMIN")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void changeUserRolePatch_SelfRoleChange() throws Exception {
        doNothing().when(authService).checkAdmin(2L);

        mockMvc.perform(patch("/api/admin/users/2/role")
                .param("newRole", "USER")
                .param("requesterId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No puedes cambiar tu propio rol"));
    }

    @Test
    void getSystemStats_Success() throws Exception {
        doNothing().when(authService).checkAdmin(1L);
        SystemStatsDTO stats = new SystemStatsDTO(10, 100, 5000.0);
        when(accountService.getSystemStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/stats")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10));
    }

    @Test
    void exportUsersCsv_Success() throws Exception {
        doNothing().when(authService).checkAdmin(1L);
        when(exportService.exportUsersAsCsv()).thenReturn("data".getBytes());

        mockMvc.perform(get("/api/admin/users/export/csv")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"usuarios_deustobank.csv\""));
    }

    @Test
    void markAsReviewed_Success() throws Exception {
        doNothing().when(authService).checkAdmin(1L);
        SuspiciousAlert alert = new SuspiciousAlert();
        alert.setId(100L);
        when(alertRepo.findById(100L)).thenReturn(Optional.of(alert));

        mockMvc.perform(put("/api/admin/alerts/100/review")
                .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alerta marcada como revisada"));
    }
}
