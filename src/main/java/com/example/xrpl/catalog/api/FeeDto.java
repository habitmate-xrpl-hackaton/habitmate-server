package com.example.xrpl.catalog.api;

import com.example.xrpl.catalog.domain.model.Challenge;
import com.example.xrpl.catalog.domain.model.Fee;

import java.math.BigDecimal;

public record FeeDto(
        String currency,
        BigDecimal amount
) {
    public static FeeDto from(Challenge challenge) {
        Fee entreFee  = challenge.getEntryFee();
        Fee serviceFee = challenge.getServiceFee();
        BigDecimal totalAmount = entreFee.amount().add(serviceFee.amount());
        return new FeeDto(challenge.getEntryFee().currency(), totalAmount);
    }
}