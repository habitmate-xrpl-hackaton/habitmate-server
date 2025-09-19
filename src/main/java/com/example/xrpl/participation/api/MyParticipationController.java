package com.example.xrpl.participation.api;

import com.example.xrpl.participation.application.MyParticipationCommandService;
import com.example.xrpl.participation.application.MyParticipationQueryService;
import com.example.xrpl.user.api.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "My Participation", description = "내 참여 활동 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MyParticipationController {

    private final MyParticipationQueryService participationQueryService;
    private final MyParticipationCommandService participationCommandService;

    @Operation(summary = "내 참여 챌린지 목록 조회", description = "현재 내가 참여하고 있는 챌린지 목록과 진행률을 조회합니다.")
    @GetMapping("/my-participations")
    public ResponseEntity<Page<MyParticipationListDto>> findMyParticipations(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MyParticipationListDto> response = participationQueryService.findMyParticipations(user.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "챌린지 인증", description = "참여 중인 챌린지에 대한 인증을 업로드합니다.")
    @PostMapping(value = "/challenges/{challengeId}/participations/me/proofs")
    public ResponseEntity<Void> addProof(
            @Parameter(description = "챌린지 ID") @PathVariable long challengeId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user,
            @Valid @RequestBody ProofCreateRequest request
    ) {
        participationCommandService.addProof(challengeId, user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}