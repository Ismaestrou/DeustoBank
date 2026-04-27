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

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private double limiteGastoMensual = 0.0;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private double gastoMensualActual = 0.0;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private double umbralSaldoBajo = 0.0;


    // RELACIÓN CON USER (CLAVE)
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

    public double getLimiteGastoMensual() { 
        return limiteGastoMensual; 
    }
    
    public void setLimiteGastoMensual(double limiteGastoMensual) { 
        this.limiteGastoMensual = limiteGastoMensual; 
    }

    public double getGastoMensualActual() { 
        return gastoMensualActual; 
    }

    public void setGastoMensualActual(double gastoMensualActual) { 
        this.gastoMensualActual = gastoMensualActual; 
    }

    public double getUmbralSaldoBajo() { 
        return umbralSaldoBajo; 
    }

    public void setUmbralSaldoBajo(double umbralSaldoBajo) { 
        this.umbralSaldoBajo = umbralSaldoBajo; 
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}