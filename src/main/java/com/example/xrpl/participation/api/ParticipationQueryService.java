package com.example.xrpl.participation.api;

import java.util.List;

public interface ParticipationQueryService {

    List<ParticipantInfo> findParticipantsByChallengeId(long challengeId);

    record ParticipantInfo(
            Long userId,
            String escrowOwner,
            String offerSequence,
            int passedProofCount
    ) {}
}
