package com.example.xrpl.challenge.api;

import com.example.xrpl.challenge.api.dto.ChallengeCreateRequest;
import com.example.xrpl.challenge.api.dto.ChallengeCreateResponse;
import com.example.xrpl.challenge.api.dto.ChallengeListDto;
import com.example.xrpl.challenge.application.ChallengeService;
import com.example.xrpl.challenge.domain.model.ChallengeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.xrpl.challenge.api.dto.ChallengeDetailDto;

@Tag(name = "Challenge", description = "챌린지 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다.")
    @PostMapping("/challenges")
    public ResponseEntity<ChallengeCreateResponse> createChallenge(@RequestBody ChallengeCreateRequest createRequest) {
        ChallengeCreateResponse response = challengeService.createChallenge(createRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공개 챌린지 목록 조회 / 검색 가능", description = "타입, 키워드, 페이징 정보에 따라 공개된 챌린지 목록을 조회합니다.")
    @GetMapping("/public-challenges")
    public ResponseEntity<Page<ChallengeListDto>> findPublicChallenges(
            @RequestParam(required = false) ChallengeType type,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "period.startDate") Pageable pageable
    ) {
        Page<ChallengeListDto> response = challengeService.findChallenges(type, keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "챌린지 상세 조회", description = "챌린지 ID를 사용하여 특정 챌린지의 상세 정보를 조회합니다.")
    @GetMapping("/challenges/{id}")
    public ResponseEntity<ChallengeDetailDto> findChallengeDetail(@Parameter(description = "조회할 챌린지의 ID") @PathVariable Long id) {
        ChallengeDetailDto response = challengeService.findChallengeDetail(id);
        return ResponseEntity.ok(response);
    }
}
