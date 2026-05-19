package com.example.deustobank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private double amount;
    private LocalDateTime date;

    private double balanceBefore;
    private double balanceAfter;

    @ManyToOne
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({
        "user",
        "hibernateLazyInitializer",
        "handler"
    })
    private Account account;

    public Transaction(String type, double amount, Account account) {
        this.type = type;
        this.amount = amount;
        this.account = account;
        this.date = LocalDateTime.now();
    }
}