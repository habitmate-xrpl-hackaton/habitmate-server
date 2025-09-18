package com.example.xrpl.catalog.domain.model;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public record Period(
        LocalDate startDate,
        LocalDate endDate
) {
    public Period {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
