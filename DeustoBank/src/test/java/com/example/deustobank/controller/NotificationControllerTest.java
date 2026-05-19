package com.example.deustobank.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.deustobank.model.Notification;
import com.example.deustobank.service.NotificationService;
import com.example.deustobank.service.AuthService;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AuthService authService;

    private Notification notification1;
    private Notification notification2;

    @BeforeEach
    void setUp() {
        notification1 = new Notification(1L, "Transferencia recibida de 200€", "TRANSFER");
        notification1.setRead(false);

        notification2 = new Notification(1L, "Tu cuenta ha sido actualizada", "ACCOUNT");
        notification2.setRead(true);
    }

    // ============================================================
    // GET /api/notifications – todas las notificaciones del usuario
    // ============================================================

    @Test
    void getUserNotifications_AllNotifications_Success() throws Exception {

        when(notificationService.getUserNotifications(1L, false))
                .thenReturn(List.of(notification1, notification2));

        mockMvc.perform(get("/api/notifications")
                        .param("requesterId", "1")
                        .param("unreadOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].message").value("Transferencia recibida de 200€"))
                .andExpect(jsonPath("$[1].message").value("Tu cuenta ha sido actualizada"));
    }

    @Test
    void getUserNotifications_UnreadOnly_Success() throws Exception {

        when(notificationService.getUserNotifications(1L, true))
                .thenReturn(List.of(notification1));

        mockMvc.perform(get("/api/notifications")
                        .param("requesterId", "1")
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message").value("Transferencia recibida de 200€"));
    }

    @Test
    void getUserNotifications_DefaultUnreadOnly_IsFalse() throws Exception {

        when(notificationService.getUserNotifications(eq(1L), eq(false)))
                .thenReturn(List.of(notification1, notification2));

        // No se pasa unreadOnly → debe usar el valor por defecto false
        mockMvc.perform(get("/api/notifications")
                        .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserNotifications_EmptyList_Success() throws Exception {

        when(notificationService.getUserNotifications(anyLong(), anyBoolean()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications")
                        .param("requesterId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ============================================================
    // PUT /api/notifications/{id}/read – marcar notificación como leída
    // ============================================================

    @Test
    void markAsRead_Success() throws Exception {

        notification1.setRead(true);

        when(notificationService.markAsRead(1L, 1L))
                .thenReturn(notification1);

        mockMvc.perform(put("/api/notifications/1/read")
                        .param("requesterId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.message").value("Transferencia recibida de 200€"));
    }

    @Test
    void markAsRead_NotificationNotFound_ThrowsException() {

        when(notificationService.markAsRead(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Notificación no encontrada"));

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(put("/api/notifications/999/read")
                        .param("requesterId", "1")));

        assertTrue(ex.getCause() instanceof RuntimeException);
        assertTrue(ex.getCause().getMessage().contains("Notificación no encontrada"));
    }

    @Test
    void markAsRead_Unauthorized_ThrowsException() {

        when(notificationService.markAsRead(1L, 2L))
                .thenThrow(new RuntimeException("No autorizado"));

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(put("/api/notifications/1/read")
                        .param("requesterId", "2")));

        assertTrue(ex.getCause() instanceof RuntimeException);
        assertTrue(ex.getCause().getMessage().contains("No autorizado"));
    }
}