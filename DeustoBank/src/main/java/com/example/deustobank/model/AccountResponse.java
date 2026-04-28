package com.example.deustobank.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AccountResponse {
    private Account account;
    private String alert;
}
