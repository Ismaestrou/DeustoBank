package com.example.deustobank.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AccountModelTest {

    @Test
    void hasDebt_NegativeBalance_ReturnsTrue() {
        Account account = new Account();
        account.setBalance(-0.01);
        assertTrue(account.hasDebt());
    }

    @Test
    void hasDebt_ZeroBalance_ReturnsFalse() {
        Account account = new Account();
        account.setBalance(0.0);
        assertFalse(account.hasDebt());
    }

    @Test
    void hasDebt_PositiveBalance_ReturnsFalse() {
        Account account = new Account();
        account.setBalance(100.0);
        assertFalse(account.hasDebt());
    }
}