package com.example.xrpl.catalog.domain.model;

import lombok.Getter;

@Getter
public enum ChallengeStatus {
    RECRUITING("모집중"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료됨");

    private final String status;

    ChallengeStatus(String status) {
        this.status =  status;
    }
}