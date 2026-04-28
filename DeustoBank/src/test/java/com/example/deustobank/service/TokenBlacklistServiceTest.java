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
    void invalidateAndIsBlacklisted() {
        String token = "my-test-token";
        
        assertFalse(service.isBlacklisted(token));
        
        service.invalidate(token);
        
        assertTrue(service.isBlacklisted(token));
    }
}
