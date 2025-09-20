package com.example.xrpl.user.api;

import com.example.xrpl.user.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String providerKey, Long userId, Role role, String xrplAddress, String xrplSecret, Boolean isKYC, String issuerAddress, String credentialType) {
        Date expiryDate = Date.from(Instant.now().plusMillis(accessTokenExpireTime));
        return createToken(providerKey, userId, role, expiryDate, xrplAddress, xrplSecret, isKYC, issuerAddress, credentialType);
    }

    public String createRefreshToken(String providerKey) {
        Date expiryDate = Date.from(Instant.now().plusMillis(refreshTokenExpireTime));
        return createToken(providerKey, null, null, expiryDate, null,null, null, null, null);
    }

    private String createToken(String providerKey, Long userId, Role role, Date expiryDate, String xrplAddress, String xrplSecret, Boolean isKYC, String issuerAddress, String credentialType) {
        JwtBuilder builder = Jwts.builder()
                .subject(providerKey)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(key);

        if (role != null) {
            builder.claim("role", role.name());
        }
        if (userId != null) {
            builder.claim("userId", userId);
        }
        if (xrplAddress != null) {
            builder.claim("xrplAddress", xrplAddress);
        }
        if (xrplSecret != null) {
            builder.claim("xrplSecret", xrplSecret);
        }
        if (isKYC != null) {
            builder.claim("isKYC", isKYC);
        }
        if (issuerAddress != null) {
            builder.claim("issuerAddress", issuerAddress);
        }
        if (credentialType != null) {
            builder.claim("credentialType", credentialType);
        }

        return builder.compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: 서명 오류, 형식 오류, 만료 등 모든 JWT 관련 예외의 상위 클래스
            // IllegalArgumentException: 토큰이 null이거나 비어있을 경우 등
            return false;
        }
    }

    public String getProviderKeyFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload(); // getBody()도 동일하게 동작합니다.
    }
}