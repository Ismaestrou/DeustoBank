package com.example.deustobank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del titular es obligatorio")
    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false)
    private double monthlySpendingLimit = 0.0;

    // 🔥 RELACIÓN CON USER (CLAVE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Account() {}

    public boolean hasDebt() {
        return this.balance < 0;
    }

    // GETTERS & SETTERS

    public Long getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getMonthlySpendingLimit() { 
        return monthlySpendingLimit; 
    }
    
    public void setMonthlySpendingLimit(double monthlySpendingLimit) { 
        this.monthlySpendingLimit = monthlySpendingLimit; 
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}