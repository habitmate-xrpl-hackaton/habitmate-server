package com.example.xrpl.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/oauth2/authorization")
@RequiredArgsConstructor
public class AuthController {

    private final TokenRefreshService tokenRefreshService;

    @Operation(
            summary = "Google OAuth2 로그인",
            description = """
                     Google 로그인 페이지로 리디렉션하여 인증을 시작합니다.
                     
                     **로그인 성공 시 전체 흐름:**
                     1. 사용자는 Google 로그인 및 동의 과정을 거칩니다.
                     2. Google은 사용자를 백엔드의 콜백 URI(`.../login/oauth2/code/google`)로 리디렉션합니다.
                     3. 백엔드는 인증 정보를 바탕으로 서비스의 Access Token과 Refresh Token을 발급합니다.
                     4. 최종적으로 사용자는 발급된 토큰을 쿼리 파라미터로 포함한 특정 URL로 리디렉션됩니다.
                     
        
                     **인증 토큰 사용법:**
        
                     Google 로그인 성공 후 발급받은 Access Token은 만료되기 전까지 모든 인증이 필요한 API 요청에 사용해야 합니다.
                     HTTP 요청 헤더의 `Authorization` 필드에 `Bearer {ACCESS_TOKEN}` 형식으로 토큰을 포함하여 전송하세요.
                     """
    )
    @GetMapping("/google")
    public void googleLogin() {
    }

    @Operation(summary = "Access Token 재발급", description = "유효한 Refresh Token을 사용하여 만료된 Access Token을 재발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshAccessToken(@RequestBody TokenRefreshRequest request) {
        String newAccessToken = tokenRefreshService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken));
    }
}