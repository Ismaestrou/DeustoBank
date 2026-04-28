package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

    private TokenBlacklistService service;

    @BeforeEach
   void setUp() {
        service = new TokenBlacklistService();
    }

    @Test
    void invalidateAndCheckToken() {
        String token = "my-test-token";

        // Al inicio NO está invalidado
        assertFalse(service.isInvalidated(token));

        // Lo invalidamos
        service.invalidate(token);

        // Ahora sí debe estar invalidado
        assertTrue(service.isInvalidated(token));
    }
}