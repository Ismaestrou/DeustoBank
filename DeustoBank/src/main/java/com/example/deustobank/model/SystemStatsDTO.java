package com.example.deustobank.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class SystemStatsDTO {

    private long totalUsers;
    private long totalTransactions;
    private double totalBalance;

}