package com.example.xrpl.challenge.api;

import com.example.xrpl.challenge.api.dto.ChallengeCreateRequest;
import com.example.xrpl.challenge.application.ChallengeService;
import com.example.xrpl.challenge.domain.event.ChallengeCreatedEvent;
import com.example.xrpl.challenge.domain.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApplicationModuleTest
@AutoConfigureMockMvc
@DisplayName("챌린지 생성 API 통합 테스트")
@TestPropertySource(properties = {
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class ChallengeApiIntegrationTest {

    @TestConfiguration
    @EnableScheduling // 스케줄러 활성화
    static class TestConfig {
        @Primary
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/**").permitAll()
                            .anyRequest().authenticated());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChallengeService challengeService;

    @Nested
    @DisplayName("챌린지 생성 요청 시")
    class CreateChallengeTest {

        @Test
        @DisplayName("성공: 유효한 데이터로 요청하면 200 OK와 함께 생성된 챌린지 정보를 반환하고, 관련 이벤트가 발행되며, DB에 저장된다.")
        void createChallenge_withValidRequest_shouldSucceed(Scenario scenario) throws Exception {
            // given
            ChallengeCreateRequest request = new ChallengeCreateRequest(
                    ChallengeType.PERSONAL,
                    "매일 1만보 걷기",
                    "건강을 위한 첫걸음, 매일 1만보 걷기에 도전하세요!",
                    Category.FITNESS,
                    Difficulty.EASY,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(31),
                    ProofFrequency.SEVEN_TIMES_A_WEEK,
                    new Fee("XRP", new BigDecimal("10")),
                    ProofType.PHOTO,
                    List.of("걸음 수 측정 앱 스크린샷 첨부", "하루에 한 번만 인증 가능")
            );

            // when
            scenario
                    .stimulate(() -> {
                        challengeService.createChallenge(request);
                    })
                    // then
                    .andWaitForEventOfType(ChallengeCreatedEvent.class)
                    .toArriveAndVerify((event) -> {
                        assertThat(event.challenge().getTitle()).isEqualTo("매일 1만보 걷기");
                    });
        }

        @Test
        @DisplayName("실패: 필수 필드(title)가 누락된 요청으로 생성 시 400 Bad Request를 반환한다.")
        void createChallenge_withMissingTitle_shouldReturnBadRequest() throws Exception {
            // given
            ChallengeCreateRequest request = new ChallengeCreateRequest(
                    ChallengeType.PERSONAL,
                    null, // title is missing
                    "건강을 위한 첫걸음, 매일 1만보 걷기에 도전하세요!",
                    Category.FITNESS,
                    Difficulty.EASY,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(31),
                    ProofFrequency.SEVEN_TIMES_A_WEEK,
                    new Fee("XRP", new BigDecimal("10")),
                    ProofType.PHOTO,
                    List.of("걸음 수 측정 앱 스크린샷 첨부", "하루에 한 번만 인증 가능")
            );

            // when & then
            mockMvc.perform(post("/api/v1/challenges")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}