package com.example.xrpl.catalog.api;

import com.example.xrpl.catalog.domain.model.Challenge;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChallengeCommandService {

    private final ChallengeRepository challengeRepository;

    /**
     * 챌린지에 참가하고 참가자 수를 1 증가시킵니다.
     * 이 메서드는 부모 트랜잭션(챌린지 참가 서비스)에 참여합니다.
     *
     * @param challengeId 참가할 챌린지 ID
     * @throws EntityNotFoundException 챌린지를 찾을 수 없는 경우
     * @throws IllegalStateException 챌린지 참가 조건(모집중, 정원 미달)을 만족하지 못하는 경우
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void participateInChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found with id: " + challengeId));

        challenge.participate();
    }
}