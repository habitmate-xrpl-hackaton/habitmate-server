package com.example.xrpl.catalog.api;

import com.example.xrpl.catalog.domain.model.Challenge;

import java.math.BigDecimal;

public record FeeDto(
        String currency,
        BigDecimal amount
) {
    public static FeeDto fromEntryFee(Challenge challenge) {
        return new FeeDto(challenge.getEntryFee().currency(), challenge.getEntryFee().amount());
    }

    public static FeeDto fromServiceFee(Challenge challenge) {
        return new FeeDto(challenge.getServiceFee().currency(), challenge.getServiceFee().amount());
    }
}