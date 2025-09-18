package com.example.xrpl.participation.api;

import com.example.xrpl.catalog.domain.model.*;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import com.example.xrpl.participation.domain.model.Proof;
import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
import com.example.xrpl.security.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ApplicationModuleTest(extraIncludes = {"catalog", "user"})
@AutoConfigureMockMvc
@Transactional
class MyParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @BeforeEach
    void setUp() {
        challengeParticipantRepository.deleteAll();
        challengeRepository.deleteAll();
    }

    private Challenge createAndSaveChallenge(String title, LocalDate startDate, LocalDate endDate, int frequency) {
        Challenge challenge = Challenge.of(
                new com.example.xrpl.catalog.api.ChallengeCreateRequest(
                        ChallengeType.SOLO, title, "description", Category.SOCIAL, Difficulty.EASY,
                        startDate, endDate, ProofFrequency.fromTimes(frequency),
                        new Fee("XRP", BigDecimal.TEN), new Fee("XRP", BigDecimal.ZERO),
                        ProofType.PHOTO, List.of("rule1"), 1
                )
        );
        return challengeRepository.save(challenge);
    }

    @DisplayName("사용자가 참여중인 챌린지 목록을 페이징하여 조회한다.")
    @Test
    @WithMockCustomUser
    void findMyParticipations_Success() throws Exception {
        // given
        final long testUserId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 14); // 14일 (2주)

        // 1. Catalog 모듈의 챌린지 정보 저장
        Challenge challenge1 = createAndSaveChallenge("Challenge 1", startDate, endDate, 3);
        Challenge challenge2 = createAndSaveChallenge("Challenge 2", startDate, endDate, 7);

        // 2. 참여 정보 저장 (챌린지 ID: challenge1.getId(), 인증 2개)
        Proof proof1_1 = Proof.of(LocalDateTime.now(), true, "url1");
        Proof proof1_2 = Proof.of(LocalDateTime.now(), true, "url2");
        ChallengeParticipant participant1 = ChallengeParticipant.of(challenge1.getId(), testUserId);
        participant1.addProof(proof1_1);
        participant1.addProof(proof1_2);
        challengeParticipantRepository.save(participant1);

        // 3. 참여 정보 저장 (챌린지 ID: challenge2.getId(), 인증 1개)
        Proof proof2_1 = Proof.of(LocalDateTime.now(), true, "url3");
        ChallengeParticipant participant2 = ChallengeParticipant.of(challenge2.getId(), testUserId);
        participant2.addProof(proof2_1);
        challengeParticipantRepository.save(participant2);

        // when & then
        mockMvc.perform(get("/api/v1/my-participations")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                // 챌린지 101번 검증
                .andExpect(jsonPath("$.content[?(@.id == " + challenge2.getId() + ")].title").value("Challenge 2"))
                .andExpect(jsonPath("$.content[?(@.id == " + challenge2.getId() + ")].totalParticipatingCount").value(1)) // 저장된 인증 수
                .andExpect(jsonPath("$.content[?(@.id == " + challenge2.getId() + ")].totalProofCount").value(14)) // 2주 * 7회 = 14회
                // 챌린지 102번 검증
                .andExpect(jsonPath("$.content[?(@.id == " + challenge1.getId() + ")].title").value("Challenge 1"))
                .andExpect(jsonPath("$.content[?(@.id == " + challenge1.getId() + ")].totalParticipatingCount").value(2)) // 저장된 인증 수
                .andExpect(jsonPath("$.content[?(@.id == " + challenge1.getId() + ")].totalProofCount").value(6)); // 2주 * 3회 = 6회
    }
}
