package com.example.deustobank.model;

public class SystemStatsDTO {

    private long totalUsers;
    private long totalTransactions;
    private double totalBalance;

    public SystemStatsDTO(long totalUsers, long totalTransactions, double totalBalance) {
        this.totalUsers = totalUsers;
        this.totalTransactions = totalTransactions;
        this.totalBalance = totalBalance;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getTotalTransactions() { return totalTransactions; }
    public double getTotalBalance() { return totalBalance; }
}