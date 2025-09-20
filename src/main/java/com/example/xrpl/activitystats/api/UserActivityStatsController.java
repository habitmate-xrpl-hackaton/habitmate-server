package com.example.xrpl.activitystats.api;

import com.example.xrpl.activitystats.application.UserActivityStatsQueryService;
import com.example.xrpl.user.api.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity-stats")
@RequiredArgsConstructor
public class UserActivityStatsController {

    private final UserActivityStatsQueryService userActivityStatsQueryService;

    @GetMapping("/me")
    public ResponseEntity<UserActivityStatsDto> getMyActivityStats(@AuthenticationPrincipal CustomOAuth2User user) {
        UserActivityStatsDto statsDto = userActivityStatsQueryService.findByUserId(user.getUserId());
        return ResponseEntity.ok(statsDto);
    }
}