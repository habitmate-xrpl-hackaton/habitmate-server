package com.example.xrpl.participation.api;

import com.example.xrpl.participation.application.NewMemberParticipationService;
import com.example.xrpl.user.api.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Challenge Participation", description = "챌린지 참가 관련 API")
@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class NewMemberParticipationController {

    private final NewMemberParticipationService newMemberParticipationService;

    @Operation(summary = "챌린지 참가", description = "특정 챌린지에 참가합니다. 성공 시 참가 정보가 생성됩니다.")
    @PostMapping("/{challengeId}/participations")
    public ResponseEntity<Void> participateInChallenge(
            @Parameter(description = "참가할 챌린지 ID") @PathVariable Long challengeId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        newMemberParticipationService.participateInChallenge(challengeId, user.getUserId());
        return ResponseEntity.ok().build();
    }
}