package com.example.xrpl.challenge.api.dto;

import com.example.xrpl.challenge.domain.model.Challenge;
import com.example.xrpl.challenge.domain.model.ChallengeType;
import com.example.xrpl.challenge.domain.model.Fee;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
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
        @JsonProperty("duration_days")
        long durationDays,
        @JsonProperty("participants_count")
        int participantsCount,
        @JsonProperty("participation_fee")
        Fee participationFee
) {
    public static ChallengeDetailDto from(Challenge challenge) {
        List<String> tags = new ArrayList<>();
        Optional.ofNullable(challenge.getDifficulty()).ifPresent(d -> tags.add(d.name()));
        Optional.ofNullable(challenge.getCategory()).ifPresent(c -> tags.add(c.name()));

        Fee calculatedParticipationFee;
        if (challenge.getType() == ChallengeType.SOLO) {
            calculatedParticipationFee = challenge.getEntryFee();
        } else {
            BigDecimal totalAmount = challenge.getEntryFee().amount()
                    .add(Optional.ofNullable(challenge.getServiceFee())
                            .map(Fee::amount)
                            .orElse(BigDecimal.ZERO));
            calculatedParticipationFee = new Fee(challenge.getEntryFee().currency(), totalAmount);
        }

        return new ChallengeDetailDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                tags,
                challenge.getPeriod().startDate(),
                ChronoUnit.DAYS.between(challenge.getPeriod().startDate(), challenge.getPeriod().endDate()),
                challenge.getParticipants().size(),
                calculatedParticipationFee
        );
    }
}
