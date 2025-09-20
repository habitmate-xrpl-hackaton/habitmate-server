package com.example.xrpl.participation.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChallengeParticipantCreatedEvent {
    private long challengeId;
}
