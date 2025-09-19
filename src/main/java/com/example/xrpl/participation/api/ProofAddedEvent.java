package com.example.xrpl.participation.api;

public record ProofAddedEvent(
        Long challengeId,
        Long participantId,
        Long proofId,
        String imageUrl
) {
}
