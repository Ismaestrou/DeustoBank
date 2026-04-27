package com.example.deustobank.model;

public class AccountResponse {
    private Account account;
    private String alert;

    public AccountResponse(Account account, String alert) {
        this.account = account;
        this.alert = alert;
    }

    public Account getAccount() { 
        return account; 
    }

    public String getAlert() { 
        return alert; 
    }
    
}
