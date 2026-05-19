package com.example.deustobank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.deustobank.model.CategorySummaryDTO;
import com.example.deustobank.model.TransactionCategory;
import com.example.deustobank.service.CategoryService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public TransactionCategory[] getCategories() {
        return TransactionCategory.values();
    }

    @PutMapping("/transactions/{txId}/category")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long txId,
            @RequestParam Long accountId,
            @RequestParam TransactionCategory category) {
        try {
            return ResponseEntity.ok(categoryService.updateCategory(txId, accountId, category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/accounts/{accountId}/summary")
    public ResponseEntity<?> getSummary(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            if (from.isAfter(to)) {
                return ResponseEntity.badRequest().body("La fecha inicio no puede ser posterior a la fecha fin");
            }
            List<CategorySummaryDTO> summary = categoryService.getSummaryByCategory(accountId, from, to);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}