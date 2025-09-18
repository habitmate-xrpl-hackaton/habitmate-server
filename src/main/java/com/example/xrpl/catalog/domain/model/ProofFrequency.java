package com.example.xrpl.catalog.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@Getter
public enum ProofFrequency {
    ONCE_A_WEEK(1),
    TWICE_A_WEEK(2),
    THREE_TIMES_A_WEEK(3),
    FOUR_TIMES_A_WEEK(4),
    FIVE_TIMES_A_WEEK(5),
    SIX_TIMES_A_WEEK(6),
    SEVEN_TIMES_A_WEEK(7);

    private final int times;

    ProofFrequency(int times) {
        this.times = times;
    }

    @JsonCreator
    public static ProofFrequency fromTimes(int times) {
        return Arrays.stream(ProofFrequency.values())
                .filter(frequency -> frequency.times == times)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid frequency: " + times));
    }

    @JsonValue
    public int getTimes() {
        return times;
    }

    /**
     * 주어진 챌린지 기간(Period) 동안 수행해야 할 총 인증 횟수를 계산합니다.
     * <p>
     * 계산 로직:
     * 1. 챌린지 기간의 총 일수를 계산합니다.
     * 2. 총 일수를 7로 나누어 전체 주(week)의 수와 나머지 일수를 구합니다.
     * 3. (전체 주 * 주당 횟수) + min(나머지 일수, 주당 횟수) 공식을 사용하여 총 인증 횟수를 계산합니다.
     *
     * @param period 챌린지 기간
     * @return 총 인증 횟수
     */
    public int getTotalProofCount(Period period) {
        if (period == null || period.startDate() == null || period.endDate() == null) {
            return 0;
        }
        long totalDays = ChronoUnit.DAYS.between(period.startDate(), period.endDate()) + 1;
        long baseWeeks = totalDays / 7;
        long extraDays = totalDays % 7;

        return (int) (baseWeeks * this.times + Math.min(extraDays, this.times));
    }
}
