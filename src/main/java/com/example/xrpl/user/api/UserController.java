package com.example.xrpl.user.api;

import com.example.xrpl.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 다른 사용자를 팔로우하거나 언팔로우합니다.
     *
     * @param targetUserId  팔로우/언팔로우 대상 사용자의 ID
     * @param currentUser   현재 로그인한 사용자의 ID
     * @return 성공 응답 (200 OK)
     */
    @PostMapping("/{targetUserId}/follow")
    public ResponseEntity<Void> toggleFollow(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal CustomOAuth2User currentUser) {
        userService.toggleFollow(currentUser.getUserId(), targetUserId);
        return ResponseEntity.ok().build();
    }
}
