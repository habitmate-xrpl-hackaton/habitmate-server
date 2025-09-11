package com.example.xrpl.challenge.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record VerificationRule(
        @Enumerated(EnumType.STRING)
        ProofType proofType,

        @Enumerated(EnumType.STRING)
        ProofFrequency frequency
) {
}