package com.example.xrpl.user.api;

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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String providerKey = customOAuth2User.getName();
        Long userId = customOAuth2User.getUserId();

        String accessToken = jwtTokenProvider.createAccessToken(providerKey, userId, customOAuth2User.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(providerKey);

        log.info("OAuth2 Login successful for user: {}, Issued Access Token", customOAuth2User.getName());

        String targetUrl = UriComponentsBuilder.fromUriString("https://n.news.naver.com/article/092/0002390755?cds=news_media_pc")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}