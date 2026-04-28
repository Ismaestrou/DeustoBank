package com.example.deustobank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "accounts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del titular es obligatorio")
    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private double limiteGastoMensual = 0.0;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private double gastoMensualActual = 0.0;

    @Column(nullable = false, columnDefinition = "float default 0.0")
    private double umbralSaldoBajo = 0.0;

    // RELACIÓN CON USER (CLAVE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean hasDebt() {
        return this.balance < 0;
    }
}