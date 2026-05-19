package com.example.deustobank.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.CategorySummaryDTO;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.model.TransactionCategory;
import com.example.deustobank.repository.AccountRepository;
import com.example.deustobank.repository.TransactionRepository;
import com.example.deustobank.repository.UserRepository;
import com.example.deustobank.service.AccountService;
import com.example.deustobank.service.AlertService;
import com.example.deustobank.service.AuthService;
import com.example.deustobank.service.CategoryService;
import com.example.deustobank.service.ExportService;
import com.example.deustobank.service.NotificationService;
import com.example.deustobank.service.PdfService;
import com.example.deustobank.service.ScheduledTransferService;
import com.example.deustobank.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- MockBeans requeridos por el contexto ---
    @MockBean private CategoryService categoryService;
    @MockBean private AccountService accountService;
    @MockBean private AuthService authService;
    @MockBean private AlertService alertService;
    @MockBean private NotificationService notificationService;
    @MockBean private ExportService exportService;
    @MockBean private PdfService pdfService;
    @MockBean private UserService userService;
    @MockBean private ScheduledTransferService scheduledTransferService;
    @MockBean private UserRepository userRepository;
    @MockBean private TransactionRepository transactionRepository;
    @MockBean private AccountRepository accountRepository;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setId(1L);

        testTransaction = new Transaction("WITHDRAW", 50.0, account);
        testTransaction.setCategory(TransactionCategory.ALIMENTACION);
    }

    // ============================================================
    // GET /categories — getCategories
    // ============================================================

    @Test
    void getCategories_ReturnsAllValues() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ============================================================
    // PUT /categories/transactions/{txId}/category — updateCategory
    // ============================================================

    @Test
    void updateCategory_Success() throws Exception {
        when(categoryService.updateCategory(eq(1L), eq(1L), eq(TransactionCategory.OCIO)))
                .thenReturn(testTransaction);

        mockMvc.perform(put("/categories/transactions/1/category")
                        .param("accountId", "1")
                        .param("category", "OCIO"))
                .andExpect(status().isOk());
    }

    @Test
    void updateCategory_Error() throws Exception {
        when(categoryService.updateCategory(eq(99L), eq(1L), any(TransactionCategory.class)))
                .thenThrow(new RuntimeException("Transacción no encontrada"));

        mockMvc.perform(put("/categories/transactions/99/category")
                        .param("accountId", "1")
                        .param("category", "OCIO"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transacción no encontrada"));
    }

    @Test
    void updateCategory_WrongAccount_Error() throws Exception {
        when(categoryService.updateCategory(eq(1L), eq(99L), any(TransactionCategory.class)))
                .thenThrow(new RuntimeException("La transacción no pertenece a esta cuenta"));

        mockMvc.perform(put("/categories/transactions/1/category")
                        .param("accountId", "99")
                        .param("category", "SALUD"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La transacción no pertenece a esta cuenta"));
    }

    // ============================================================
    // GET /categories/accounts/{accountId}/summary — getSummary
    // ============================================================

    @Test
    void getSummary_Success() throws Exception {
        List<CategorySummaryDTO> summary = List.of(
                new CategorySummaryDTO(TransactionCategory.ALIMENTACION, 150.0, 3L)
        );
        when(categoryService.getSummaryByCategory(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(summary);

        mockMvc.perform(get("/categories/accounts/1/summary")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("ALIMENTACION"))
                .andExpect(jsonPath("$[0].totalAmount").value(150.0));
    }

    @Test
    void getSummary_InvalidDateRange_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/categories/accounts/1/summary")
                        .param("from", "2024-02-01")
                        .param("to", "2024-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La fecha inicio no puede ser posterior a la fecha fin"));
    }

    @Test
    void getSummary_ServiceError_ReturnsBadRequest() throws Exception {
        when(categoryService.getSummaryByCategory(eq(99L), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Cuenta no encontrada"));

        mockMvc.perform(get("/categories/accounts/99/summary")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cuenta no encontrada"));
    }

    @Test
    void getSummary_EmptyResult() throws Exception {
        when(categoryService.getSummaryByCategory(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/categories/accounts/1/summary")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}