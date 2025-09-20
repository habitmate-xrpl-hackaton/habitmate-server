package com.example.xrpl.participation.api;

public record ProofStatusUpdatedEvent(
        Long proofId,
        Long userId,
        boolean isSuccess) {
}