package com.example.xrpl.participation.api;

public record ProofStatusUpdatedEvent(
        Long proofId,
        boolean isSuccess
) {
}