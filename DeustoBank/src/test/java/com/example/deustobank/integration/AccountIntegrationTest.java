package com.example.deustobank.integration;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.User;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Long userId;
    private Long accountId;

    @BeforeEach
    void setUp() {

        baseUrl = "http://localhost:" + port;

        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuario
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("1234");
        user.setDni("12345678A");
        user.setFullName("Test User");
        user.setRole("USER");
        user.setActive(true);

        userId = userRepository.save(user).getId();

        // Crear cuenta
        Account account = new Account();
        account.setOwnerName("Test User");
        account.setBalance(100);
        account.setUser(user);

        accountId = accountRepository.save(account).getId();
    }

    @Test
    void deposit_endpoint_shouldUpdateBalance() {

        RestTemplate restTemplate = new RestTemplate();

        // Llamada real al endpoint
        restTemplate.put(
            baseUrl + "/accounts/" + accountId + "/deposit?amount=50&requesterId=" + userId,
            null
        );

        // Verificación en base de datos
        Account updated = accountRepository.findById(accountId).orElseThrow();

        assertEquals(150, updated.getBalance());
    }
}