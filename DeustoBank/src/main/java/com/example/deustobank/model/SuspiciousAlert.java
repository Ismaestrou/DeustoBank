package com.example.deustobank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suspicious_alerts")
@Data
@NoArgsConstructor
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

    public SuspiciousAlert(AlertType alertType, String description, Account account) {
        this.alertType   = alertType;
        this.description = description;
        this.account     = account;
        this.createdAt   = LocalDateTime.now();
        this.reviewed    = false;
    }
}