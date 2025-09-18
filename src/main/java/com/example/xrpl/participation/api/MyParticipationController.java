package com.example.xrpl.participation.api;

import com.example.xrpl.participation.application.ParticipationQueryService;
import com.example.xrpl.user.api.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "My Participation", description = "내 참여 활동 관련 API")
@RestController
@RequestMapping("/api/v1/my-participations")
@RequiredArgsConstructor
public class MyParticipationController {

    private final ParticipationQueryService participationQueryService;

    @Operation(summary = "내 참여 챌린지 목록 조회", description = "현재 내가 참여하고 있는 챌린지 목록과 진행률을 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<MyParticipationListDto>> findMyParticipations(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user,
            @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<MyParticipationListDto> response = participationQueryService.findMyParticipations(user.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }
}