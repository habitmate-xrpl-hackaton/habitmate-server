package com.example.xrpl.catalog.domain.model;

import com.example.xrpl.catalog.domain.converter.ProofFrequencyConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record VerificationRule(
        @Enumerated(EnumType.STRING)
        ProofType proofType,

        @Convert(converter = ProofFrequencyConverter.class)
        ProofFrequency frequency
) {
}