package com.example.deustobank.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "scheduled_transfers")
public class ScheduledTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    private double amount;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private LocalDate startDate;
    private LocalDate nextExecution;
    private boolean active = true;

    public enum Frequency { DAILY, WEEKLY, MONTHLY }

    // Getters y Setters
    public Long getId() { return id; }
    public Account getFromAccount() { return fromAccount; }
    public void setFromAccount(Account fromAccount) { this.fromAccount = fromAccount; }
    public Account getToAccount() { return toAccount; }
    public void setToAccount(Account toAccount) { this.toAccount = toAccount; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getNextExecution() { return nextExecution; }
    public void setNextExecution(LocalDate nextExecution) { this.nextExecution = nextExecution; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}