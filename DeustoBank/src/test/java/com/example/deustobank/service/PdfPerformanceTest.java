package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.Transaction;

@SpringBootTest
class PdfPerformanceTest {

    @Autowired
    private PdfService pdfService;

    @Test
    void testPdfGenerationWithLargeVolumeOfData() {
        Account acc = new Account();
        acc.setId(1L);
        acc.setOwnerName("Usuario Rendimiento");
        acc.setBalance(50000.0);

        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            Transaction t = new Transaction("DEPOSIT", 10.0, acc);
            t.setBalanceBefore(i * 10.0);
            t.setBalanceAfter((i + 1) * 10.0);
            transactions.add(t);
        }

        long startTime = System.currentTimeMillis();
        byte[] pdfBytes = pdfService.generateStatement(acc, transactions);
        long endTime = System.currentTimeMillis();

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        long duration = endTime - startTime;
        assertTrue(duration < 5000, "La generación de PDF tardó demasiado: " + duration + " ms");
        System.out.println("PDF con 5000 transacciones generado en: " + duration + " ms");
    }
}
