package com.example.xrpl.catalog.api;

import com.example.xrpl.catalog.domain.model.Challenge;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ChallengeDetailDto(
        long id,
        String title,
        String description,
        List<String> tag,
        @JsonProperty("start_date")
        LocalDate startDate,
        @JsonProperty("end_date")
        LocalDate endDate,
        @JsonProperty("duration_days")
        long durationDays,
        @JsonProperty("participants_count")
        int participantsCount,
        @JsonProperty("entry_fee")
        FeeDto entryFee,
        @JsonProperty("service_fee")
        FeeDto serviceFee,
        @JsonProperty("challenge_rules")
        List<String> rules
) {
    public static ChallengeDetailDto from(Challenge challenge) {
        List<String> tags = new ArrayList<>();
        Optional.ofNullable(challenge.getDifficulty()).ifPresent(d -> tags.add(d.name()));
        Optional.ofNullable(challenge.getCategory()).ifPresent(c -> tags.add(c.name()));

        return new ChallengeDetailDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                tags,
                challenge.getPeriod().startDate(),
                challenge.getPeriod().endDate(),
                ChronoUnit.DAYS.between(challenge.getPeriod().startDate(), challenge.getPeriod().endDate()),
                challenge.getCurrentParticipantsCount(),
                FeeDto.fromEntryFee(challenge),
                FeeDto.fromServiceFee(challenge),
                challenge.getRules()
        );
    }
}
