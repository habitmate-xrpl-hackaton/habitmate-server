package com.example.xrpl.challenge.domain.model;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public record Fee (
        String currency,
        BigDecimal amount
) {
    public Fee  {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
}
