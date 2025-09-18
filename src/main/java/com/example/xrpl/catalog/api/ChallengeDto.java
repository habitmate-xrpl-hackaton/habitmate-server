package com.example.xrpl.catalog.api;

import java.time.LocalDate;

public record ChallengeDto(
        Long id,
        String title,
        FeeDto entryFee,
        LocalDate startDate,
        LocalDate endDate
) {
}