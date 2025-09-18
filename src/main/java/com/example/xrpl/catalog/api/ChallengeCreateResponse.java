package com.example.xrpl.catalog.api;

import com.example.xrpl.catalog.domain.model.ChallengeStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ChallengeCreateResponse(
        @JsonProperty("challenge_id")
        Long id,
        ChallengeStatus status
) {
}
