package com.example.deustobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.deustobank.model.Notification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}
