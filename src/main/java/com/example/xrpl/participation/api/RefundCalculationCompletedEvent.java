package com.example.xrpl.participation.api;

import java.util.List;

public record RefundCalculationCompletedEvent(
        long challengeId,
        List<Long> successfulParticipantIds
) {
}
