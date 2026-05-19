package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.deustobank.model.Notification;
import com.example.deustobank.repository.NotificationRepository;
import com.example.deustobank.event.NotificationEvent;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        Notification notification = new Notification(event.getUserId(), event.getMessage(), event.getType());
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId, boolean unreadOnly) {
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(Long notificationId, Long requesterId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        if (!notification.getUserId().equals(requesterId)) {
            throw new RuntimeException("No autorizado");
        }
        
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
}
