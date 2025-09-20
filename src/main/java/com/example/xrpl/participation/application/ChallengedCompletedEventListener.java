package com.example.xrpl.participation.application;

import com.example.xrpl.catalog.api.ChallengeCompletedEvent;
import com.example.xrpl.participation.ChallengeInfoProvider;
import com.example.xrpl.participation.api.RefundCalculationCompletedEvent;
import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengedCompletedEventListener {

    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeInfoProvider challengeInfoProvider;
    private final ApplicationEventPublisher eventPublisher;

    @ApplicationModuleListener
    public void handleChallengeCompletedEvent(ChallengeCompletedEvent event) {
        log.info("Handling ChallengeCompletedEvent for challengeId: {}", event.challengeId());

        // 1. SPI를 통해 외부 모듈(catalog)로부터 총 인증 횟수 정보를 가져옵니다.
        ChallengeInfoProvider.ChallengeInfo challengeInfo = challengeInfoProvider.findChallengeInfoById(event.challengeId())
                .orElseThrow(() -> new IllegalStateException("Challenge info not found for id: " + event.challengeId()));

        List<ChallengeParticipant> participants = challengeParticipantRepository.findByChallengeId(event.challengeId());

        if (participants.isEmpty()) {
            log.info("No participants found for challengeId: {}. Publishing event with empty list.", event.challengeId());
            eventPublisher.publishEvent(new RefundCalculationCompletedEvent(event.challengeId(), Collections.emptyList()));
            return;
        }

        int totalProofCount = challengeInfo.totalProofCount();

        // 2. 성공률 70% 이상인 참가자 ID 목록을 추립니다.
        List<Long> successfulParticipantIds = participants.stream()
                .filter(participant -> calculateSuccessRate(participant, totalProofCount) >= 0.7)
                .map(ChallengeParticipant::getId)
                .toList();

        log.info("Found {} successful participants for challengeId: {}. Publishing RefundCalculationCompletedEvent.",
                successfulParticipantIds.size(), event.challengeId());

        // 3. 환급 대상자 목록을 담아 다음 이벤트를 발행합니다.
        eventPublisher.publishEvent(new RefundCalculationCompletedEvent(event.challengeId(), successfulParticipantIds));
    }

    /**
     * 참가자의 인증 성공률을 계산합니다.
     * @param participant 참가자 애그리게이트
     * @param totalProofCount 챌린지의 총 인증 필요 횟수
     * @return 성공률 (0.0 ~ 1.0)
     */
    private double calculateSuccessRate(ChallengeParticipant participant, int totalProofCount) {
        if (totalProofCount == 0) {
            return 0.0;
        }
        // 위에서 추가한 getPassedProofCount() 메서드를 사용합니다.
        int passedCount = participant.getPassedProofCount();
        return (double) passedCount / totalProofCount;
    }
}
