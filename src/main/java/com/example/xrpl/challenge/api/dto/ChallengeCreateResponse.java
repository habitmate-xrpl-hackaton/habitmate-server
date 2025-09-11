package com.example.xrpl.challenge.api.dto;

import com.example.xrpl.challenge.domain.model.ChallengeStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ChallengeCreateResponse(
        @JsonProperty("challenge_id")
        Long id,
        ChallengeStatus status
) {
}
