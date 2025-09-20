package com.example.xrpl.catalog.api;

import com.example.xrpl.xrpl.application.XRPLService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import org.junit.jupiter.api.BeforeEach;
import com.example.xrpl.catalog.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class ChallengeControllerTest {

    @Mock
    private XRPLService xrplService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChallengeRepository challengeRepository;

    @BeforeEach
    void setUp() {
        challengeRepository.deleteAll();
    }

    private Challenge createAndSaveChallenge(String title, ChallengeType type, LocalDate startDate, LocalDate endDate) {
        ChallengeCreateRequest request = new ChallengeCreateRequest(
                type,
                title,
                "description for " + title,
                Category.LEARNING,
                Difficulty.EASY,
                startDate,
                endDate,
                ProofFrequency.SEVEN_TIMES_A_WEEK,
                new Fee("XRP", new BigDecimal("10000.543131")),
                new Fee("XRP", new BigDecimal("100.123131")),
                ProofType.PHOTO,
                List.of("rule1", "rule2"),
                100
        );
        Challenge challenge = Challenge.of(request);
        return challengeRepository.save(challenge);
    }

    @DisplayName("새로운 챌린지를 생성한다.")
    @Test
    void createChallenge() throws Exception {
        // given
        ChallengeCreateRequest request = new ChallengeCreateRequest(
                ChallengeType.SOLO,
                "매일 아침 5시에 일어나기",
                "아침 5시에 일어나서 인증샷을 남기는 챌린지",
                Category.LEARNING,
                Difficulty.EASY,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10),
                ProofFrequency.SEVEN_TIMES_A_WEEK,
                new Fee("XRP", new BigDecimal("10000.543131")),
                new Fee("XRP", new BigDecimal("100.123131")),
                ProofType.PHOTO,
                List.of("5시 정각에 일어나서 본인의 얼굴이 나오도록 사진을 찍어야 합니다."),
                100
        );

        // when // then
        mockMvc.perform(post("/api/v1/challenges")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge_id").exists())
                .andExpect(jsonPath("$.status").value("RECRUITING"));
    }

    @DisplayName("챌린지 목록을 조회한다.")
    @Test
    void findPublicChallenges() throws Exception {
        // given
        LocalDate today = LocalDate.now();
        createAndSaveChallenge("챌린지1", ChallengeType.SOLO, today.plusDays(1), today.plusDays(10));
        createAndSaveChallenge("챌린지2", ChallengeType.GROUP, today.plusDays(2), today.plusDays(11));
        createAndSaveChallenge("챌린지3", ChallengeType.SOLO, today.plusDays(3), today.plusDays(12));

        // when // then
        mockMvc.perform(get("/api/v1/public-challenges")
                        .param("type", ChallengeType.SOLO.name())
                        .param("keyword", "챌린지")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "period.startDate,desc")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("챌린지3"))
                .andExpect(jsonPath("$.content[1].title").value("챌린지1"));
    }

    @DisplayName("큐레이션된 챌린지 목록을 타입별로 최신순으로 조회한다.")
    @Test
    void findCuratedChallengesByType() throws Exception {
        // given
        LocalDate today = LocalDate.now();
        createAndSaveChallenge("솔로챌린지", ChallengeType.SOLO, today.plusDays(1), today.plusDays(10));
        createAndSaveChallenge("그룹챌린지-오래됨", ChallengeType.GROUP, today.minusDays(5), today.plusDays(5));
        createAndSaveChallenge("브랜드챌린지-최신", ChallengeType.BRAND, today.plusDays(2), today.plusDays(12));
        createAndSaveChallenge("그룹챌린지-최신", ChallengeType.GROUP, today.plusDays(3), today.plusDays(13));
        createAndSaveChallenge("브랜드챌린지-오래됨", ChallengeType.BRAND, today.minusDays(1), today.plusDays(9));

        // when & then: GROUP 타입 챌린지 조회 (최신순)
        mockMvc.perform(get("/api/v1/curated-challenges")
                        .param("type", ChallengeType.GROUP.name())
                        .param("page", "0")
                        .param("size", "10")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("그룹챌린지-최신"))
                .andExpect(jsonPath("$.content[1].title").value("그룹챌린지-오래됨"));

        // when & then: BRAND 타입 챌린지 조회 (최신순)
        mockMvc.perform(get("/api/v1/curated-challenges")
                        .param("type", ChallengeType.BRAND.name())
                        .param("page", "0")
                        .param("size", "10")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("브랜드챌린지-최신"))
                .andExpect(jsonPath("$.content[1].title").value("브랜드챌린지-오래됨"));

        // when & then: SOLO 타입으로 요청 시 Bad Request
        mockMvc.perform(get("/api/v1/curated-challenges")
                        .param("type", ChallengeType.SOLO.name())
                        .param("page", "0")
                        .param("size", "10")
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("챌린지 상세 정보를 조회한다.")
    @Test
    @WithMockUser
    void findChallengeDetail() throws Exception {
        // given
        LocalDate today = LocalDate.now();
        Challenge challenge = createAndSaveChallenge("상세조회-챌린지", ChallengeType.SOLO, today.plusDays(1), today.plusDays(21));

        // when & then: 정상 조회
        mockMvc.perform(get("/api/v1/challenges/{id}", challenge.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(challenge.getId()))
                .andExpect(jsonPath("$.title").value("상세조회-챌린지"))
                .andExpect(jsonPath("$.description").value("description for 상세조회-챌린지"))
                .andExpect(jsonPath("$.tag[0]").value(challenge.getDifficulty().name()))
                .andExpect(jsonPath("$.tag[1]").value(challenge.getCategory().name()))
                .andExpect(jsonPath("$.start_date").value(challenge.getPeriod().startDate().toString()))
                .andExpect(jsonPath("$.duration_days").value(ChronoUnit.DAYS.between(challenge.getPeriod().startDate(), challenge.getPeriod().endDate())))
                .andExpect(jsonPath("$.participants_count").value(1))
                .andExpect(jsonPath("$.entry_fee.amount").value(challenge.getEntryFee().amount()))
                .andExpect(jsonPath("$.entry_fee.currency").value(challenge.getEntryFee().currency()))
                .andExpect(jsonPath("$.service_fee.amount").value(challenge.getServiceFee().amount()))
                .andExpect(jsonPath("$.service_fee.currency").value(challenge.getServiceFee().currency()));

        // when & then: 존재하지 않는 챌린지 조회
        mockMvc.perform(get("/api/v1/challenges/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
