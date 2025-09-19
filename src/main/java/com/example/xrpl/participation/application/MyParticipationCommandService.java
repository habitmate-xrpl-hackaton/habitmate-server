package com.example.xrpl.participation.application;

import com.example.xrpl.participation.api.ProofCreateRequest;

public interface MyParticipationCommandService {
    void addProof(long challengeId, long userId, ProofCreateRequest request);
    void verifyProof(Long participantId, Long proofId, boolean isSuccess);
}
