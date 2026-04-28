package com.example.deustobank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suspicious_alerts")
public class SuspiciousAlert {

    public enum AlertType {
        HIGH_FREQUENCY,   // Muchas transferencias en poco tiempo
        HIGH_AMOUNT       // Importe inusualmente alto
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean reviewed = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public SuspiciousAlert() {}

    public SuspiciousAlert(AlertType alertType, String description, Account account) {
        this.alertType   = alertType;
        this.description = description;
        this.account     = account;
        this.createdAt   = LocalDateTime.now();
        this.reviewed    = false;
    }

    public Long getId() { return id; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}