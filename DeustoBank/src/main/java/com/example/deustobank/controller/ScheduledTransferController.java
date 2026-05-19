package com.example.deustobank.controller;

import com.example.deustobank.model.ScheduledTransfer;
import com.example.deustobank.service.ScheduledTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/scheduled-transfers")
@CrossOrigin
public class ScheduledTransferController {

    @Autowired private ScheduledTransferService service;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam Long fromId,
            @RequestParam Long toId,
            @RequestParam double amount,
            @RequestParam ScheduledTransfer.Frequency frequency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam Long requesterId) {
        try {
            return ResponseEntity.ok(service.create(fromId, toId, amount, frequency, startDate, requesterId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> edit(
            @PathVariable Long id,
            @RequestParam double amount,
            @RequestParam ScheduledTransfer.Frequency frequency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextExecution,
            @RequestParam Long requesterId) {
        try {
            return ResponseEntity.ok(service.edit(id, amount, frequency, nextExecution, requesterId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestParam Long requesterId) {
        try {
            service.cancel(id, requesterId);
            return ResponseEntity.ok("Transferencia programada cancelada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<ScheduledTransfer>> getByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(service.getByAccount(accountId));
    }
}