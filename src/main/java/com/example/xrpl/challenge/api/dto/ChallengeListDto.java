package com.example.xrpl.challenge.api.dto;

import com.example.xrpl.challenge.domain.model.Challenge;
import com.example.xrpl.challenge.domain.model.Fee;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalDate;

public record ChallengeListDto(
        long id,
        String title,
        @JsonProperty("participants_count")
        int participantsCount,
        @JsonProperty("duration_days")
        long duration,
        @JsonProperty("start_date")
        LocalDate startDate,
        @JsonProperty("end_date")
        LocalDate endDate,
        @JsonProperty("entry_fee")
        Fee entryFee,
        @JsonProperty("service_fee")
        Fee serviceFee

) {

    public static ChallengeListDto from(Challenge challenge) {
        return new ChallengeListDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getParticipants().size(),
                Duration.between(challenge.getPeriod().startDate().atStartOfDay(), challenge.getPeriod().endDate().atStartOfDay()).toDays(),
                challenge.getPeriod().startDate(),
                challenge.getPeriod().endDate(),
                challenge.getEntryFee(),
                challenge.getServiceFee()
        );
    }
}
