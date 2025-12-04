package com.wexapp.purchaseapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 50)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchaseAmount;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String currencyCode; // Stores country_currency_desc (e.g., "Canada-Dollar")

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
    }
}

