package com.example.xrpl.catalog.api;

public interface ChallengeCommandService {

    /**
     * 지정된 챌린지의 참여자 수를 1 증가시킵니다.
     * @param challengeId 챌린지 ID
     */
    void increaseParticipantCount(long challengeId);
}
