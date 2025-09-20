package com.example.xrpl.participation.application;

import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewMemberParticipationServiceImpl implements NewMemberParticipationService {

    private final ChallengeParticipantRepository participantRepository;

    @Transactional
    @Override
    public void participateInChallenge(Long challengeId, Long userId, String escrowOwner, String offerSequence) {
        ChallengeParticipant participant = ChallengeParticipant.of(challengeId, userId, escrowOwner, offerSequence);
        participantRepository.save(participant);
    }

    /**
     * 챌린지 참가 요청을 처리합니다.
     * 1. catalog 모듈의 서비스를 호출하여 챌린지 참가자 수를 증가시킵니다. (정원 초과 시 예외 발생)
     * 2. participation 모듈에서 ChallengeParticipant 애그리게이트를 생성하고 저장합니다.
     * 이 모든 과정은 단일 트랜잭션으로 처리되어 데이터 정합성을 보장합니다.
     *
     * @param challengeId 참가할 챌린지 ID
     * @param userId      참가하는 사용자 ID
     */
    @Transactional
    public void participateInChallenge(Long challengeId, Long userId) {
        ChallengeParticipant participant = ChallengeParticipant.of(challengeId, userId);
        participantRepository.save(participant);
    }
}