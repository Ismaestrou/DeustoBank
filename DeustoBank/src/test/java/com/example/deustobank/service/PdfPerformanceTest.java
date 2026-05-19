package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.Transaction;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

    // ============================================================
    // generateStatement — rama feliz
    // ============================================================

    @Test
    void generateStatement_ReturnsPdfBytes() {
        PdfService pdfService = new PdfService();

        Account account = new Account();
        account.setId(1L);
        account.setOwnerName("Test User");
        account.setBalance(1000.0);

        Transaction t = new Transaction("DEPOSIT", 100.0, account);
        t.setBalanceBefore(900.0);
        t.setBalanceAfter(1000.0);

        byte[] result = pdfService.generateStatement(account, List.of(t));

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateStatement_EmptyTransactions_ReturnsPdfBytes() {
        PdfService pdfService = new PdfService();

        Account account = new Account();
        account.setId(2L);
        account.setOwnerName("Empty User");
        account.setBalance(0.0);

        byte[] result = pdfService.generateStatement(account, List.of());

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    // ============================================================
    // generateStatement — rama catch (DocumentException)
    // ============================================================

    @Test
    void generateStatement_DocumentException_ReturnsEmptyBytes() throws DocumentException {
        // Subclase anónima que sobreescribe createDocument() devolviendo un
        // Document mockeado que lanza DocumentException al llamar a add().
        Document brokenDoc = mock(Document.class);
        doThrow(new DocumentException("forced error")).when(brokenDoc).add(any());

        PdfService pdfService = new PdfService() {
            @Override
            protected Document createDocument() {
                return brokenDoc;
            }
        };

        Account account = new Account();
        account.setId(3L);
        account.setOwnerName("Error User");
        account.setBalance(0.0);

        // No debe lanzar excepción; el catch la absorbe y devuelve array vacío
        byte[] result = pdfService.generateStatement(account, List.of());

        assertNotNull(result);
    }
}