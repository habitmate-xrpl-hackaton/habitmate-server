package com.example.xrpl.participation.api;

import com.example.xrpl.catalog.api.FeeDto;

import java.time.LocalDate;

public record MyParticipationListDto(
        Long id,
        String title,
        long totalParticipatingCount,
        long totalProofCount,
        FeeDto entryFee,
        LocalDate startDate,
        LocalDate endDate
) {
}
