package com.example.xrpl.participation.application;

import com.example.xrpl.catalog.api.ChallengeQueryService;
import com.example.xrpl.participation.api.AiProofVerifier;
import com.example.xrpl.participation.api.ProofAddedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class ProofAddedListener {

    private static final Logger logger = LoggerFactory.getLogger(ProofAddedListener.class);
    private final AiProofVerifier aiProofVerifier;
    private final ChallengeQueryService challengeQueryService; // Repository 대신 QueryService 사용
    private final ObjectMapper objectMapper;

    @ApplicationModuleListener
    public void handleProofAdded(ProofAddedEvent event) {
        try {
            // 1. catalog 모듈의 API를 통해 챌린지 정보를 조회합니다.
            var challengeInfo = challengeQueryService.findChallengeById(event.challengeId());

            // 2. AI에게 전달할 컨텍스트를 JSON 문자열로 변환합니다.
            String challengeContextAsJson = objectMapper.writeValueAsString(challengeInfo);

            // 3. 같은 모듈의 비동기 컴포넌트에 데이터를 전달하고 트랜잭션을 종료합니다.
            aiProofVerifier.verifyProof(event, challengeContextAsJson);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize Challenge DTO to JSON for challengeId: {}", event.challengeId(), e);
        } catch (NoSuchElementException e) {
            logger.error("Challenge from event not found in database. challengeId: {}", event.challengeId(), e);
        }
    }
}
