package com.example.xrpl.challenge.domain.model;

import lombok.Getter;

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
}
