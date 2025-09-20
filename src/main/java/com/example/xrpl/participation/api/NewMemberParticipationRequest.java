package com.example.xrpl.participation.api;

public record NewMemberParticipationRequest(
        String escrowOwner,
        String offerSequence
) {
}
