package com.example.xrpl.catalog.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public record Fee (
        String currency,
        BigDecimal amount
) {
    @JsonCreator
    public Fee(@JsonProperty("currency") String currency, @JsonProperty("amount") BigDecimal amount)  {
        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.currency = currency;
        this.amount = amount;
    }
}
