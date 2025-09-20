package com.example.xrpl.activitystats.api;

import com.example.xrpl.activitystats.domain.UserActivityStats;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserActivityStatsDto {

    private Long id;
    private Long userId;
    private int consecutiveSuccessDays;
    private int maxConsecutiveSuccessDays;
    private long totalProofCount;
    private long successProofCount;
    private LocalDate lastSuccessDate;
    private int point;

    public static UserActivityStatsDto from(UserActivityStats stats) {
        if (stats == null) {
            return null;
        }
        return UserActivityStatsDto.builder()
                .id(stats.getId())
                .userId(stats.getUserId())
                .consecutiveSuccessDays(stats.getConsecutiveSuccessDays())
                .maxConsecutiveSuccessDays(stats.getMaxConsecutiveSuccessDays())
                .totalProofCount(stats.getTotalProofCount())
                .successProofCount(stats.getSuccessProofCount())
                .lastSuccessDate(stats.getLastSuccessDate())
                .point(stats.getPoint())
                .build();
    }
}