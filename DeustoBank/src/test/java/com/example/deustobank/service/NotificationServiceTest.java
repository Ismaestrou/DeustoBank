package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import java.util.List;

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

    @Test
    void testHandleNotificationEvent() {
        NotificationEvent event = new NotificationEvent(this, 1L, "Mensaje", "INFO");
        notificationService.handleNotificationEvent(event);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testGetUserNotifications() {
        Notification n1 = new Notification(1L, "Test", "INFO");
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(n1));
        
        List<Notification> result = notificationService.getUserNotifications(1L, false);
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getMessage());
    }

    @Test
    void testMarkAsRead() {
        Notification n = new Notification(1L, "Test", "INFO");
        n.setId(10L);
        n.setRead(false);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenReturn(n);

        Notification updated = notificationService.markAsRead(10L, 1L);

        assertTrue(updated.isRead());
        verify(notificationRepository, times(1)).save(n);
    }
}
