//package com.example.xrpl.participation.api;
//
//import com.example.xrpl.catalog.api.ChallengeCreateRequest;
//import com.example.xrpl.catalog.domain.model.*;
//import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
//import com.example.xrpl.participation.domain.model.ChallengeParticipant;
//import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
//import com.example.xrpl.security.WithMockCustomUser;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.modulith.test.ApplicationModuleTest;
//import org.springframework.modulith.test.PublishedEvents;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Collections;
//import java.util.List;
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ApplicationModuleTest(extraIncludes = {"catalog", "user"})
//@AutoConfigureMockMvc
//class MyParticipationControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private ChallengeParticipantRepository challengeParticipantRepository;
//
//    @Autowired
//    private ChallengeRepository challengeRepository;
//
//    @Autowired
//    private PublishedEvents events;
//
//    @MockBean
//    private AiProofVerifier aiProofVerifier;
//
//    @BeforeEach
//    void setUp() {
//        challengeParticipantRepository.deleteAll();
//        challengeRepository.deleteAll();
//    }
//
//    private Challenge createAndSaveChallenge(String title, String description, List<String> rules, int frequency) {
//        ChallengeCreateRequest request = new ChallengeCreateRequest(
//                ChallengeType.SOLO, title, description, Category.FITNESS, Difficulty.EASY,
//                LocalDate.now(), LocalDate.now().plusWeeks(2), ProofFrequency.fromTimes(frequency),
//                new Fee("XRP", BigDecimal.TEN), new Fee("XRP", BigDecimal.ZERO),
//                ProofType.PHOTO, rules, 1
//        );
//        Challenge challenge = Challenge.of(request);
//        return challengeRepository.save(challenge);
//    }
//
//    @DisplayName("사용자가 참여중인 챌린지 목록을 페이징하여 조회한다.")
//    @Test
//    @WithMockCustomUser
//    @Transactional
//    void findMyParticipations_Success() throws Exception {
//        // given
//        final long testUserId = 1L;
//
//        // 1. Catalog 모듈의 챌린지 정보 저장
//        Challenge challenge1 = createAndSaveChallenge(
//                "Challenge 1",
//                "챌린지 1입니다",
//                List.of(
//                "1. The proof shot must be a photo of a running tracker app screen showing the distance.",
//                "2. You can also upload a photo of your running shoes on a track or trail."),
//                3);
//        Challenge challenge2 = createAndSaveChallenge("Challenge 2",
//                "챌린지 2입니다",
//                List.of(
//                "1. The proof shot must be a photo of a running tracker app screen showing the distance.",
//                "2. You can also upload a photo of your running shoes on a track or trail."),
//                7);
//
//        // 2. 참여 정보 저장 (챌린지 ID: challenge1.getId())
//        ChallengeParticipant participant1 = challengeParticipantRepository.save(ChallengeParticipant.of(challenge1.getId(), testUserId));
//        // 저장 후 다시 조회하여 Proof 추가 (ID가 할당된 상태에서 이벤트 발행을 위함)
//        ChallengeParticipant savedParticipant1 = challengeParticipantRepository.findById(participant1.getId()).get();
//        savedParticipant1.addProof("url1", "desc1", Collections.emptySet());
//        savedParticipant1.addProof("url2", "desc2", Collections.emptySet());
//        challengeParticipantRepository.save(savedParticipant1);
//
//        // 3. 참여 정보 저장 (챌린지 ID: challenge2.getId())
//        ChallengeParticipant participant2 = challengeParticipantRepository.save(ChallengeParticipant.of(challenge2.getId(), testUserId));
//        ChallengeParticipant savedParticipant2 = challengeParticipantRepository.findById(participant2.getId()).get();
//        savedParticipant2.addProof("url3", "desc3", Collections.emptySet());
//        challengeParticipantRepository.save(savedParticipant2);
//
//        // when & then
//        mockMvc.perform(get("/api/v1/my-participations")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .param("sort", "id,desc")
//                )
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content").isArray())
//                .andExpect(jsonPath("$.content.length()").value(2))
//                .andExpect(jsonPath("$.totalElements").value(2))
//                // 챌린지 101번 검증
//                .andExpect(jsonPath("$.content[?(@.id == " + challenge2.getId() + ")].title").value("Challenge 2"))
//                .andExpect(jsonPath("$.content[?(@.id == " + challenge2.getId() + ")].totalParticipatingCount").value(1)) // 저장된 인증 수
//                .andExpect(jsonPath("$.content[?(@.id == " + challenge2.getId() + ")].totalProofCount").value(15)) // 2주 * 7회 = 14회
//                // 챌린지 102번 검증
//                .andExpect(jsonPath("$.content[?(@.id == " + challenge1.getId() + ")].title").value("Challenge 1"))
//                .andExpect(jsonPath("$.content[?(@.id == " + challenge1.getId() + ")].totalParticipatingCount").value(2)) // 저장된 인증 수
//                .andExpect(jsonPath("$.content[?(@.id == " + challenge1.getId() + ")].totalProofCount").value(7)); // 2주 * 3회 = 6회
//    }
//
//    @DisplayName("[통합 테스트] '달리기 챌린지'에 실제 이미지 URL로 인증을 성공적으로 추가한다.")
//    @Test
//    @WithMockCustomUser
//    void addProof_forRunningChallenge_Success() throws Exception {
//        // given
//        final long testUserId = 1L;
//        Challenge runningChallenge = createAndSaveChallenge(
//                "Daily 3km Running Challenge",
//                "Run at least 3km every day and upload a proof shot.",
//                List.of(
//                        "1. The proof shot must be a photo of a running tracker app screen showing the distance.",
//                        "2. You can also upload a photo of your running shoes on a track or trail."),
//                7
//        );
//        ChallengeParticipant participant = ChallengeParticipant.of(runningChallenge.getId(), testUserId);
//        challengeParticipantRepository.save(participant);
//
//        ProofCreateRequest request = new ProofCreateRequest(
//                "https://example.com/running.jpeg",
//                "Finished my 3km run today! Felt amazing.",
//                Set.of("running", "morningrun", "health")
//        );
//
//        // when
//        mockMvc.perform(post("/api/v1/challenges/{challengeId}/participations/me/proofs", runningChallenge.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isCreated());
//
//        // then
//        var proofAddedEvents = events.ofType(ProofAddedEvent.class);
//        assertThat(proofAddedEvents).hasSize(1);
//
//        var proofAddedEvent = proofAddedEvents.stream().findFirst().get();
//        assertThat(proofAddedEvent.challengeId()).isEqualTo(runningChallenge.getId());
//        assertThat(proofAddedEvent.imageUrl()).isEqualTo(request.imageUrl());
//    }
//}
