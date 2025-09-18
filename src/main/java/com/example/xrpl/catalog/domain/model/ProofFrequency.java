package com.example.xrpl.catalog.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

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
}
