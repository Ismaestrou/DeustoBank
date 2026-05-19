package com.example.deustobank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.example.deustobank.model.Notification;
import com.example.deustobank.service.NotificationService;
import com.example.deustobank.service.AuthService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            @RequestParam Long requesterId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        
        List<Notification> notifications = notificationService.getUserNotifications(requesterId, unreadOnly);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long id,
            @RequestParam Long requesterId) {
        
        Notification notification = notificationService.markAsRead(id, requesterId);
        return ResponseEntity.ok(notification);
    }
}
