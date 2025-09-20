package com.example.xrpl.user.api;

import com.example.xrpl.xrpl.api.XRPLTestWalletService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final XRPLTestWalletService xrplTestWalletService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String providerKey = customOAuth2User.getName();
        Long userId = customOAuth2User.getUserId();

        String accessToken = jwtTokenProvider.createAccessToken(providerKey, userId, customOAuth2User.getRole(), customOAuth2User.getXrplAddress(), customOAuth2User.getXrplSecret(), customOAuth2User.getIsKYC());
        String refreshToken = jwtTokenProvider.createRefreshToken(providerKey);

        log.info("OAuth2 Login successful for user: {}, Issued Access Token", customOAuth2User.getName());

        String targetUrl = UriComponentsBuilder.fromUriString("https://habitmate-client-mu.vercel.app/ ")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}