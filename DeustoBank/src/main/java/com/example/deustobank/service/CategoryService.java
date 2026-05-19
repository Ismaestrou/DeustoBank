package com.example.deustobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.deustobank.model.CategorySummaryDTO;
import com.example.deustobank.model.Transaction;
import com.example.deustobank.model.TransactionCategory;
import com.example.deustobank.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private TransactionRepository transactionRepo;

    private static final Map<TransactionCategory, List<String>> KEYWORDS = Map.of(
        TransactionCategory.ALIMENTACION, List.of(
            "alimentacion", "supermercado", "mercadona", "lidl", "aldi", "carrefour", "eroski",
            "restaurante", "cafeteria", "bar", "pizza", "comida"
        ),
        TransactionCategory.TRANSPORTE, List.of(
            "transporte", "gasolina", "gasolinera", "repsol", "bp", "cepsa", "metro", "bus",
            "tren", "renfe", "taxi", "uber", "cabify", "peaje", "parking"
        ),
        TransactionCategory.OCIO, List.of(
            "ocio", "cine", "teatro", "concierto", "spotify", "netflix", "amazon prime",
            "hbo", "videojuego", "steam", "gym", "gimnasio", "viaje", "hotel"
        ),
        TransactionCategory.SALUD, List.of(
            "salud", "farmacia", "medico", "hospital", "clinica", "dentista", "medicamento", "optica"
        ),
        TransactionCategory.EDUCACION, List.of(
            "educacion", "universidad", "colegio", "academia", "curso", "libro", "matricula", "escuela"
        ),
        TransactionCategory.HOGAR, List.of(
            "hogar", "alquiler", "hipoteca", "luz", "agua", "gas", "internet", "telefono",
            "seguro hogar", "mueble", "ikea", "leroy"
        ),
        TransactionCategory.NOMINA, List.of(
            "nomina", "salario", "sueldo", "paga"
        ),
        TransactionCategory.TRANSFERENCIA, List.of(
            "transferencia", "bizum", "envio"
        )
    );

    public TransactionCategory categorize(String concepto, String type) {
        if ("TRANSFER_IN".equals(type) || "TRANSFER_OUT".equals(type)) {
            if (concepto == null || concepto.isBlank()) {
                return TransactionCategory.TRANSFERENCIA;
            }
        }
        if (concepto == null || concepto.isBlank()) {
            return TransactionCategory.OTROS;
        }
        String lower = concepto.toLowerCase();
        for (Map.Entry<TransactionCategory, List<String>> entry : KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return TransactionCategory.OTROS;
    }

    public void autoCategorize(Transaction transaction) {
        transaction.setCategory(categorize(transaction.getConcepto(), transaction.getType()));
    }

    public Transaction updateCategory(Long transactionId, Long accountId, TransactionCategory newCategory) {
        Transaction transaction = transactionRepo.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
        if (!transaction.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("La transacción no pertenece a esta cuenta");
        }
        transaction.setCategory(newCategory);
        return transactionRepo.save(transaction);
    }

    public List<CategorySummaryDTO> getSummaryByCategory(Long accountId, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt   = to.atTime(23, 59, 59);
        List<Object[]> rows  = transactionRepo.sumByCategory(accountId, fromDt, toDt);
        return rows.stream()
            .map(row -> new CategorySummaryDTO(
                (TransactionCategory) row[0],
                ((Number) row[1]).doubleValue(),
                ((Number) row[2]).longValue()
            ))
            .collect(Collectors.toList());
    }
}