package com.example.deustobank.model;

public class CategorySummaryDTO {

    private TransactionCategory category;
    private double totalAmount;
    private long transactionCount;

    public CategorySummaryDTO(TransactionCategory category, double totalAmount, long transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public TransactionCategory getCategory() { return category; }
    public double getTotalAmount()           { return totalAmount; }
    public long getTransactionCount()        { return transactionCount; }
}