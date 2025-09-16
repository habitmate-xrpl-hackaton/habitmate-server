package com.example.xrpl.challenge.api.dto;

import com.example.xrpl.challenge.domain.model.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record ChallengeCreateRequest(
        ChallengeType type,

        String title,

        String description,

        Category category,

        Difficulty difficulty,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate,

        @JsonProperty("proof_frequency")
        ProofFrequency frequency,

        @JsonProperty("entry_fee")
        Fee entryFee,

        @JsonProperty("service_fee")
        Fee serviceFee,

        @JsonProperty("proof_type")
        ProofType proofType,

        List<String> rules,

        @JsonProperty("max_participants")
        int maxParticipants
) {
}
