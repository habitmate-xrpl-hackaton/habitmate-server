package com.example.xrpl.user.application.auth;

import com.example.xrpl.user.application.auth.JwtTokenProvider;
import com.example.xrpl.user.domain.User;
import com.example.xrpl.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 유효한 Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
     *
     * @param refreshToken 재발급에 사용할 Refresh Token
     * @return 새로 발급된 Access Token
     * @throws AuthenticationException Refresh Token이 유효하지 않거나 해당 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid Refresh Token") {};
        }

        String providerKey = jwtTokenProvider.getProviderKeyFromToken(refreshToken);

        User user = userRepository.findByProviderKey(providerKey)
                .orElseThrow(() -> new AuthenticationException("User not found with providerKey: " + providerKey) {});

        return jwtTokenProvider.createAccessToken(user.getProviderKey(), user.getRole());
    }
}