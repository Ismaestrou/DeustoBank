package com.example.deustobank.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.ServletException;

import com.example.deustobank.model.SuspiciousAlert;
import com.example.deustobank.model.SystemStatsDTO;
import com.example.deustobank.model.User;

import com.example.deustobank.repository.SuspiciousAlertRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;

import com.example.deustobank.service.AccountService;
import com.example.deustobank.service.AlertService;
import com.example.deustobank.service.AuthService;
import com.example.deustobank.service.ExportService;
import com.example.deustobank.service.NotificationService;
import com.example.deustobank.service.PdfService;
import com.example.deustobank.service.UserService;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================
    // REPOSITORIES
    // =========================

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionRepository transactionRepo;

    @MockBean
    private SuspiciousAlertRepository alertRepo;

    // =========================
    // SERVICES
    // =========================

    @MockBean
    private AuthService authService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ExportService exportService;

    @MockBean
    private AlertService alertService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private PdfService pdfService;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(2L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setDni("12345678A");
        testUser.setRole("USER");
        testUser.setActive(true);
    }

    // ============================================================
    // getAllUsers
    // ============================================================

    @Test
    void getAllUsers_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        mockMvc.perform(
                get("/api/admin/users")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(2));
    }

    // ============================================================
    // getDebtors
    // ============================================================

    @Test
    void getDebtors_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findUsersWithDebt()).thenReturn(List.of(testUser));

        mockMvc.perform(
                get("/api/admin/users/debtors")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk());
    }

    // ============================================================
    // toggleUserStatus
    // ============================================================

    @Test
    void toggleUserStatus_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(
                put("/api/admin/users/2/status")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggleUserStatus_UserNotFound() {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServletException.class, () ->
            mockMvc.perform(
                    put("/api/admin/users/99/status")
                            .param("requesterId", "1")
            )
        );
    }

    @Test
    void toggleUserStatus_InactiveToActive() throws Exception {
        // Cubre la rama: usuario inactivo -> pasa a activo (toggle del boolean)
        testUser.setActive(false);
        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(
                put("/api/admin/users/2/status")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(true));
    }


    // ============================================================
    // changeUserRolePatch
    // ============================================================

    @Test
    void changeUserRolePatch_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(
                patch("/api/admin/users/2/role")
                        .param("newRole", "ADMIN")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void changeUserRolePatch_SelfRoleChange() throws Exception {

        doNothing().when(authService).checkAdmin(2L);

        mockMvc.perform(
                patch("/api/admin/users/2/role")
                        .param("newRole", "USER")
                        .param("requesterId", "2")
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().string("No puedes cambiar tu propio rol"));
    }

    @Test
    void changeUserRolePatch_InvalidRole() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(
                patch("/api/admin/users/2/role")
                        .param("newRole", "SUPERADMIN")
                        .param("requesterId", "1")
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Rol inválido. Valores permitidos: USER, ADMIN"));
    }

    @Test
    void changeUserRolePatch_UserNotFound() {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServletException.class, () ->
            mockMvc.perform(
                    patch("/api/admin/users/99/role")
                            .param("newRole", "ADMIN")
                            .param("requesterId", "1")
            )
        );
    }

    // ============================================================
    // getUserSummary
    // ============================================================

    @Test
    void getUserSummary_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(accountService.getTotalBalanceByUser(2L)).thenReturn(5000.0);
        when(transactionRepo.countByAccountUserId(2L)).thenReturn(10L);

        mockMvc.perform(
                get("/api/admin/users/2/summary")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalBalance").value(5000.0))
        .andExpect(jsonPath("$.transactions").value(10));
    }

    // ============================================================
    // getSystemStats
    // ============================================================

    @Test
    void getSystemStats_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        SystemStatsDTO stats = new SystemStatsDTO(10L, 100L, 5000.0);
        when(accountService.getSystemStats()).thenReturn(stats);

        mockMvc.perform(
                get("/api/admin/stats")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalUsers").value(10));
    }

    // ============================================================
    // getAlerts
    // ============================================================

    @Test
    void getAlerts_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        SuspiciousAlert alert = new SuspiciousAlert();
        alert.setId(1L);
        when(alertRepo.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(alert));

        mockMvc.perform(
                get("/api/admin/alerts")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));
    }

    // ============================================================
    // markAsReviewed
    // ============================================================

    @Test
    void markAsReviewed_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        SuspiciousAlert alert = new SuspiciousAlert();
        alert.setId(100L);
        when(alertRepo.findById(100L)).thenReturn(Optional.of(alert));

        mockMvc.perform(
                put("/api/admin/alerts/100/review")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Alerta marcada como revisada"));
    }

    @Test
    void markAsReviewed_AlertNotFound() {

        doNothing().when(authService).checkAdmin(1L);
        when(alertRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ServletException.class, () ->
            mockMvc.perform(
                    put("/api/admin/alerts/999/review")
                            .param("requesterId", "1")
            )
        );
    }

    // ============================================================
    // resetPassword
    // ============================================================

    @Test
    void resetPassword_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(authService.resetPassword(2L)).thenReturn("Deusto1!");

        mockMvc.perform(
                put("/api/admin/users/2/reset-password")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nuevaPassword").value("Deusto1!"));
    }

    // ============================================================
    // updateUser
    // ============================================================

    @Test
    void updateUser_Success_AllFields() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        String body = "{\"fullName\":\"Nuevo Nombre\",\"email\":\"nuevo@test.com\",\"phone\":\"600000000\",\"dni\":\"87654321B\"}";

        mockMvc.perform(
                put("/api/admin/users/2")
                        .param("requesterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("Nuevo Nombre"))
        .andExpect(jsonPath("$.email").value("nuevo@test.com"))
        .andExpect(jsonPath("$.phone").value("600000000"));
    }

    @Test
    void updateUser_BlankFields_SkipsUpdate() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        // fullName y email en blanco deben ignorarse; solo phone se actualiza
        String body = "{\"fullName\":\"\",\"email\":\"\",\"phone\":\"611111111\"}";

        mockMvc.perform(
                put("/api/admin/users/2")
                        .param("requesterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("Test User"))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.phone").value("611111111"));
    }

    @Test
    void updateUser_UserNotFound() {

        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String body = "{\"fullName\":\"Cualquier Nombre\"}";

        assertThrows(ServletException.class, () ->
            mockMvc.perform(
                    put("/api/admin/users/99")
                            .param("requesterId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
            )
        );
    }

    @Test
    void updateUser_OnlyFullName() throws Exception {
        // Rama: fullName presente y no blank -> se actualiza; resto ausente -> sin cambio.
        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        String body = "{\"fullName\":\"Solo Nombre\"}";

        mockMvc.perform(
                put("/api/admin/users/2")
                        .param("requesterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("Solo Nombre"))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.dni").value("12345678A"));
    }

    @Test
    void updateUser_OnlyEmail() throws Exception {
        // Rama: email presente y no blank -> se actualiza; resto ausente -> sin cambio.
        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        String body = "{\"email\":\"nuevo@email.com\"}";

        mockMvc.perform(
                put("/api/admin/users/2")
                        .param("requesterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("Test User"))
        .andExpect(jsonPath("$.email").value("nuevo@email.com"));
    }

    @Test
    void updateUser_OnlyDni() throws Exception {
        // Rama: dni presente y no blank -> se actualiza; resto ausente -> sin cambio.
        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        String body = "{\"dni\":\"99999999Z\"}";

        mockMvc.perform(
                put("/api/admin/users/2")
                        .param("requesterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("Test User"))
        .andExpect(jsonPath("$.dni").value("99999999Z"));
    }

    @Test
    void updateUser_EmptyBody_NoChanges() throws Exception {
        // Rama: ninguna clave presente -> no se actualiza nada.
        doNothing().when(authService).checkAdmin(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        String body = "{}";

        mockMvc.perform(
                put("/api/admin/users/2")
                        .param("requesterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("Test User"))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.dni").value("12345678A"));
    }


    // ============================================================
    // deleteUser
    // ============================================================

    @Test
    void deleteUser_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        doNothing().when(userService).deleteUser(2L);

        mockMvc.perform(
                delete("/api/admin/users/2")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk());
    }

    // ============================================================
    // exportUsersCsv
    // ============================================================

    @Test
    void exportUsersCsv_Success() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(exportService.exportUsersAsCsv()).thenReturn("csv-data".getBytes());

        mockMvc.perform(
                get("/api/admin/users/export/csv")
                        .param("requesterId", "1")
        )
        .andExpect(status().isOk());
    }

    @Test
    void exportUsersCsv_Exception() throws Exception {

        doNothing().when(authService).checkAdmin(1L);
        when(exportService.exportUsersAsCsv()).thenThrow(new RuntimeException("Error al exportar"));

        mockMvc.perform(
                get("/api/admin/users/export/csv")
                        .param("requesterId", "1")
        )
        .andExpect(status().isInternalServerError());
    }
}