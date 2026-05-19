package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.SuspiciousAlertRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SuspiciousAlertRepository alertRepository;

    @Transactional
    public void deleteUser(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        for (Account account : accounts) {
            alertRepository.deleteByAccountId(account.getId());
            transactionRepository.deleteByAccountId(account.getId());
        }
        accountRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
