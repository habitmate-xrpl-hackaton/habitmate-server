package com.example.xrpl.challenge.api;

import com.example.xrpl.challenge.api.dto.ChallengeCreateRequest;
import com.example.xrpl.challenge.domain.event.ChallengeCreatedEvent;
import com.example.xrpl.challenge.domain.model.Challenge;
import com.example.xrpl.challenge.domain.model.ChallengeStatus;
import com.example.xrpl.challenge.infrastructure.ChallengeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Spring Modulith의 Scenario 객체 사용법을 학습하기 위한 예제 테스트 클래스입니다.
 * 이 클래스는 실제 실행을 위한 것이 아닌, 학습 및 코드 예시 제공을 목적으로 합니다.
 */
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.STANDALONE)
@AutoConfigureMockMvc
public class ScenarioExamples {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ChallengeRepository challengeRepository; // 상태 검증을 위해 리포지토리 주입

    /**
     * 시나리오 1: API 호출 → 이벤트 발행 → 이벤트 내용과 DB 상태 동시 검증
     * 가장 기본적이고 중요한 패턴입니다.
     * stimulate()로 API를 호출하고, andWaitForEventOfType()으로 특정 이벤트를 기다린 후,
     * toArriveAndVerify()에서 이벤트의 내용과 DB의 최종 상태를 검증합니다.
     */
    @Test
    @DisplayName("예제 1: API 호출 후 이벤트와 DB 상태 검증")
    void example1_stimulateApiAndVerifyEvent(Scenario scenario) throws Exception {
        // API 호출에 필요한 요청 데이터를 준비합니다.
        ChallengeCreateRequest request = ChallengeFixtures.createValidChallengeRequest();

        scenario.stimulate(() -> {
                    // POST /api/v1/challenges 로 챌린지 생성 요청
                    try {
                        mockMvc.perform(post("/api/v1/challenges")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(ChallengeFixtures.toJson(request)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .andWaitForEventOfType(ChallengeCreatedEvent.class)
                .toArriveAndVerify(event -> {
                    // 1. 발행된 이벤트 자체의 내용을 검증합니다.
                    assertThat(event.challenge().getTitle()).isEqualTo(request.title());

                    // 2. 이벤트 발행 후, DB의 최종 상태를 검증합니다.
                    Challenge savedChallenge = challengeRepository.findById(event.challenge().getId()).orElseThrow();
                    assertThat(savedChallenge.getStatus()).isEqualTo(ChallengeStatus.RECRUITING);
                });
    }

    /**
     * 시나리오 2: API 호출 → 상태 변경 검증 (이벤트 X)
     * 이벤트가 발행되지 않는 대신, 특정 서비스의 상태가 변경되는 경우를 테스트합니다.
     * 예를 들어, 챌린지 상태를 RECRUITING -> IN_PROGRESS 로 변경하는 API를 테스트하는 경우입니다.
     */
    @Test
    @DisplayName("예제 2: API 호출 후 상태 변경 검증")
    void example2_stimulateApiAndVerifyStateChange(Scenario scenario) throws Exception {

        // 미리 챌린지를 하나 생성해 둡니다.
        Challenge challenge = challengeRepository.save(ChallengeFixtures.createChallenge());
        Long challengeId = challenge.getId();

        scenario.stimulate(() -> {
                    // PUT /api/v1/challenges/{id}/start 로 챌린지 상태 변경 요청
                    try {
                        mockMvc.perform(post("/api/v1/challenges/" + challengeId + "/start"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .andWaitForStateChange(() -> challengeRepository.findById(challengeId).orElseThrow())
                .andVerify(updatedChallenge -> {
                    // DB에서 챌린지를 다시 조회하여 상태가 변경되었는지 검증합니다.
                    assertThat(updatedChallenge.getStatus()).isEqualTo(ChallengeStatus.IN_PROGRESS);
                });
    }

    /**
     * 시나리오 3: API 호출 → 특정 이벤트가 발행되지 않음 검증
     * 잘못된 요청 등으로 인해 이벤트가 발행되지 않아야 하는 경우를 테스트합니다.
     */
    @Test
    @DisplayName("예제 3: 잘못된 요청 시 이벤트가 발행되지 않는지 검증")
    void example3_verifyEventNotPublished(Scenario scenario) throws Exception {

        // title이 null인 잘못된 요청 데이터
        ChallengeCreateRequest invalidRequest = ChallengeFixtures.createInvalidChallengeRequest();

        scenario.stimulate(() -> {
                    try {
                        mockMvc.perform(post("/api/v1/challenges")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(ChallengeFixtures.toJson(invalidRequest)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .andWaitForEventOfType(ChallengeCreatedEvent.class)
                .toArrive(); // ChallengeCreatedEvent가 발행되지 않을 것을 기대
    }

    /**
     * 시나리오 4: 비동기 처리 및 타임아웃 설정
     * 이벤트 리스너가 @Async로 동작하거나, 처리에 시간이 걸리는 경우 사용합니다.
     * customize() 메서드를 통해 대기 시간(timeout)을 기본값(1초)보다 길게 설정할 수 있습니다.
     */
    @Test
    @DisplayName("예제 4: 비동기 작업 시 타임아웃 설정")
    void example4_handlingAsyncWithTimeout(Scenario scenario) throws Exception {

        ChallengeCreateRequest request = ChallengeFixtures.createValidChallengeRequest();

        scenario.stimulate(() -> {
                    try {
                        mockMvc.perform(post("/api/v1/challenges")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(ChallengeFixtures.toJson(request)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .customize(it -> it.atMost(Duration.ofSeconds(5))) // 대기 시간을 5초로 늘림
                .andWaitForEventOfType(ChallengeCreatedEvent.class)
                .toArriveAndVerify(event -> {
                    assertThat(event.challenge().getTitle()).isEqualTo(request.title());
                });
    }
}

/**
 * 테스트에서 사용할 가짜(Fixture) 객체를 생성하는 헬퍼 클래스입니다.
 */
class ChallengeFixtures {
    public static ChallengeCreateRequest createValidChallengeRequest() {
        // ... 유효한 ChallengeCreateRequest 객체 생성 로직 ...
        return new ChallengeCreateRequest(null, "Valid Title", null, null, null, null, null, null, null, null, List.of("rule"));
    }

    public static ChallengeCreateRequest createInvalidChallengeRequest() {
        // ... 유효하지 않은 ChallengeCreateRequest 객체 생성 로직 (예: title이 null) ...
        return new ChallengeCreateRequest(null, null, null, null, null, null, null, null, null, null, null);
    }

    public static Challenge createChallenge() {
        // ... 테스트에 필요한 Challenge 엔티티 생성 로직 ...
        return Challenge.of(createValidChallengeRequest());
    }

    public static String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
