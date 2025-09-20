package com.example.xrpl.user.api;

import com.example.xrpl.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KYCController {

    private final UserService userService;

    @PutMapping("/api/v1/user/kyc")
    public ResponseEntity<Void> kyc(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        userService.updateKYC(customOAuth2User.getUserId());
        return ResponseEntity.ok().build();
    }
}