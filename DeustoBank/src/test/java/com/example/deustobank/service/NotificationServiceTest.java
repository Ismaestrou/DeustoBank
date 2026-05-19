package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import com.example.deustobank.model.Notification;
import com.example.deustobank.repository.NotificationRepository;
import com.example.deustobank.event.NotificationEvent;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ── handleNotificationEvent ───────────────────────────────────────────────

    @Test
    void handleNotificationEvent_SavesNotification() {
        NotificationEvent event = new NotificationEvent(this, 1L, "Mensaje", "INFO");
        notificationService.handleNotificationEvent(event);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ── getUserNotifications ──────────────────────────────────────────────────

    @Test
    void getUserNotifications_AllNotifications() {
        Notification n1 = new Notification(1L, "Test", "INFO");
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(n1));

        List<Notification> result = notificationService.getUserNotifications(1L, false);
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getMessage());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L);
        verify(notificationRepository, never()).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getUserNotifications_UnreadOnly() {
        Notification n = new Notification(1L, "Unread", "INFO");
        n.setRead(false);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n));

        List<Notification> result = notificationService.getUserNotifications(1L, true);
        assertEquals(1, result.size());
        verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L);
        verify(notificationRepository, never()).findByUserIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getUserNotifications_UnreadOnly_EmptyResult() {
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<Notification> result = notificationService.getUserNotifications(1L, true);
        assertTrue(result.isEmpty());
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    void markAsRead_Success() {
        Notification n = new Notification(1L, "Test", "INFO");
        n.setId(10L);
        n.setRead(false);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenReturn(n);

        Notification updated = notificationService.markAsRead(10L, 1L);

        assertTrue(updated.isRead());
        verify(notificationRepository).save(n);
    }

    @Test
    void markAsRead_NotificationNotFound_ThrowsException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class,
                () -> notificationService.markAsRead(999L, 1L));
        assertEquals("Notificación no encontrada", ex.getMessage());
    }

    @Test
    void markAsRead_DifferentUser_ThrowsException() {
        Notification n = new Notification(1L, "Test", "INFO");
        n.setId(10L);
        n.setRead(false);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        // Requester is user 2 but notification belongs to user 1
        Exception ex = assertThrows(RuntimeException.class,
                () -> notificationService.markAsRead(10L, 2L));
        assertEquals("No autorizado", ex.getMessage());
        verify(notificationRepository, never()).save(any());
    }
}