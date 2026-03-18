package com.example.deustobank.model;

import jakarta.persistence.*;

@Entity
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    private String ownerName;
    private double balance;

    public Account() {}
    
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
}