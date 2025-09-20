package com.example.xrpl.participation;

import java.util.Optional;

/**
 * 외부 모듈(e.g., catalog)로부터 챌린지 정보를 가져오기 위한 Service Provider Interface (SPI).
 * 이 인터페이스는 Participation 모듈이 소유하며, 다른 모듈에서 구현합니다.
 */
public interface ChallengeInfoProvider {

    Optional<ChallengeInfo> findChallengeInfoById(long challengeId);

    /**
     * 성공률 계산에 필요한 최소한의 챌린지 정보
     * @param totalProofCount 총 인증 필요 횟수
     */
    record ChallengeInfo(
            int totalProofCount
    ) {}
}