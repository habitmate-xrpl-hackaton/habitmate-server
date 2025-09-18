package com.example.xrpl.catalog.api;

import com.example.xrpl.catalog.domain.model.Challenge;

import java.time.LocalDate;

/**
 * Participation 모듈에 챌린지 기본 정보를 제공하기 위한 DTO
 */
public record ChallengeCatalogInfoDto(
        Long id,
        String title,
        FeeDto entryFee,
        LocalDate startDate,
        LocalDate endDate,
        int frequency
) {
    public static ChallengeCatalogInfoDto from(Challenge challenge) {
        return new ChallengeCatalogInfoDto(
                challenge.getId(),
                challenge.getTitle(),
                new FeeDto(challenge.getEntryFee().currency(), challenge.getEntryFee().amount().add(challenge.getServiceFee().amount())),
                challenge.getPeriod().startDate(),
                challenge.getPeriod().endDate(),
                challenge.getVerificationRule().frequency().getTimes()
        );
    }
}
