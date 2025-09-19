package com.example.xrpl.catalog.api;

public record ProofVerifiedEvent(
        Long participantId,
        Long proofId,
        boolean isSuccess,
        String reason
) {}
