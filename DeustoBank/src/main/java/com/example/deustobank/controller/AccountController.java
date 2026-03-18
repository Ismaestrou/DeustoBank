package com.example.deustobank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.deustobank.model.Account;
import com.example.deustobank.service.AccountService;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService service;

    @GetMapping
    public List<Account> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Account create(@RequestBody Account account) {
        return service.create(account);
    }
}