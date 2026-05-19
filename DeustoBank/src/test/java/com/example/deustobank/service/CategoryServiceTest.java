package com.example.deustobank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.CategorySummaryDTO;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.model.TransactionCategory;
import com.example.deustobank.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private TransactionRepository transactionRepo;

    @InjectMocks
    private CategoryService categoryService;

    private Account account;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);

        transaction = new Transaction("WITHDRAW", 50.0, account);
        transaction.setConcepto("supermercado mercadona");
        transaction.setCategory(TransactionCategory.OTROS);
    }

    // ============================================================
    // categorize — cada categoría por keyword
    // ============================================================

    @Test
    void categorize_Alimentacion() {
        assertEquals(TransactionCategory.ALIMENTACION,
                categoryService.categorize("Compra en mercadona", "WITHDRAW"));
    }

    @Test
    void categorize_Transporte() {
        assertEquals(TransactionCategory.TRANSPORTE,
                categoryService.categorize("Gasolina repsol", "WITHDRAW"));
    }

    @Test
    void categorize_Ocio() {
        assertEquals(TransactionCategory.OCIO,
                categoryService.categorize("Suscripción netflix", "WITHDRAW"));
    }

    @Test
    void categorize_Salud() {
        assertEquals(TransactionCategory.SALUD,
                categoryService.categorize("Farmacia San Marcos", "WITHDRAW"));
    }

    @Test
    void categorize_Educacion() {
        assertEquals(TransactionCategory.EDUCACION,
                categoryService.categorize("Matrícula universidad", "WITHDRAW"));
    }

    @Test
    void categorize_Hogar() {
        assertEquals(TransactionCategory.HOGAR,
                categoryService.categorize("Pago alquiler enero", "WITHDRAW"));
    }

    @Test
    void categorize_Nomina() {
        assertEquals(TransactionCategory.NOMINA,
                categoryService.categorize("Nomina enero", "WITHDRAW"));
    }

    @Test
    void categorize_Transferencia() {
        assertEquals(TransactionCategory.TRANSFERENCIA,
                categoryService.categorize("Bizum amigo", "WITHDRAW"));
    }

    @Test
    void categorize_Otros_ConConcepto() {
        assertEquals(TransactionCategory.OTROS,
                categoryService.categorize("Algo desconocido", "WITHDRAW"));
    }

    @Test
    void categorize_NullConcepto_ReturnsOtros() {
        assertEquals(TransactionCategory.OTROS,
                categoryService.categorize(null, "WITHDRAW"));
    }

    @Test
    void categorize_BlankConcepto_ReturnsOtros() {
        assertEquals(TransactionCategory.OTROS,
                categoryService.categorize("   ", "WITHDRAW"));
    }

    @Test
    void categorize_TransferIn_NullConcepto_ReturnsTransferencia() {
        assertEquals(TransactionCategory.TRANSFERENCIA,
                categoryService.categorize(null, "TRANSFER_IN"));
    }

    @Test
    void categorize_TransferOut_NullConcepto_ReturnsTransferencia() {
        assertEquals(TransactionCategory.TRANSFERENCIA,
                categoryService.categorize(null, "TRANSFER_OUT"));
    }

    @Test
    void categorize_TransferIn_ConConcepto_UsesKeywords() {
        // concepto no está vacío → entra en la lógica de keywords
        assertEquals(TransactionCategory.ALIMENTACION,
                categoryService.categorize("supermercado", "TRANSFER_IN"));
    }

    @Test
    void categorize_TransferOut_BlankConcepto_ReturnsTransferencia() {
        assertEquals(TransactionCategory.TRANSFERENCIA,
                categoryService.categorize("", "TRANSFER_OUT"));
    }

    // ============================================================
    // autoCategorize
    // ============================================================

    @Test
    void autoCategorize_SetsCategory() {
        transaction.setConcepto("supermercado");
        transaction.setType("WITHDRAW");

        categoryService.autoCategorize(transaction);

        assertEquals(TransactionCategory.ALIMENTACION, transaction.getCategory());
    }

    @Test
    void autoCategorize_NullConcepto_SetsOtros() {
        transaction.setConcepto(null);
        transaction.setType("WITHDRAW");

        categoryService.autoCategorize(transaction);

        assertEquals(TransactionCategory.OTROS, transaction.getCategory());
    }

    // ============================================================
    // updateCategory — éxito y errores
    // ============================================================

    @Test
    void updateCategory_Success() {
        when(transactionRepo.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepo.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = categoryService.updateCategory(1L, 1L, TransactionCategory.OCIO);

        assertEquals(TransactionCategory.OCIO, result.getCategory());
        verify(transactionRepo).save(transaction);
    }

    @Test
    void updateCategory_NotFound_Throws() {
        when(transactionRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                categoryService.updateCategory(99L, 1L, TransactionCategory.OCIO));
    }

    @Test
    void updateCategory_WrongAccount_Throws() {
        when(transactionRepo.findById(1L)).thenReturn(Optional.of(transaction));

        // transaction.account.id == 1L, pasamos accountId == 99L
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                categoryService.updateCategory(1L, 99L, TransactionCategory.OCIO));
        assertTrue(ex.getMessage().contains("no pertenece"));
    }

    // ============================================================
    // getSummaryByCategory
    // ============================================================

    @Test
    void getSummaryByCategory_ReturnsMappedList() {
        Object[] row = { TransactionCategory.ALIMENTACION, 200.0, 4L };
        List<Object[]> rows = new java.util.ArrayList<>();
        rows.add(row);
        when(transactionRepo.sumByCategory(any(), any(), any()))
                .thenReturn(rows);

        List<CategorySummaryDTO> result = categoryService.getSummaryByCategory(
                1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertEquals(1, result.size());
        assertEquals(TransactionCategory.ALIMENTACION, result.get(0).getCategory());
        assertEquals(200.0, result.get(0).getTotalAmount());
        assertEquals(4L, result.get(0).getTransactionCount());
    }

    @Test
    void getSummaryByCategory_EmptyRows_ReturnsEmptyList() {
        when(transactionRepo.sumByCategory(any(), any(), any()))
                .thenReturn(List.of());

        List<CategorySummaryDTO> result = categoryService.getSummaryByCategory(
                1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertTrue(result.isEmpty());
    }
}