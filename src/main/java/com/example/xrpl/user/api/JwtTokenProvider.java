package com.example.xrpl.user.api;

import com.example.xrpl.user.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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

    public String createAccessToken(String providerKey, Long userId, Role role, String xrplAddress, String xrplSecret) {
        return createToken(providerKey, userId, role, accessTokenExpireTime, xrplAddress, xrplSecret);
    }

    public String createRefreshToken(String providerKey) {
        return createToken(providerKey, null, null, refreshTokenExpireTime, null,null);
    }

    private String createToken(String providerKey, Long userId, Role role, long expireTime, String xrplAddress, String xrplSecret) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .subject(providerKey)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .claims(Collections.emptyMap());

        if (role != null) {
            builder.claim("role", role.name());
        }

        if (userId != null) {
            builder.claim("userId", userId);
        }

        builder.claim("xrplAddress", xrplAddress);
        builder.claim("xrplSecret", xrplSecret);

        return builder.compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | IllegalArgumentException e) {
            // SecurityException: 서명 오류, MalformedJwtException, ExpiredJwtException 등 jjwt의 모든 검증 예외 포함
            // IllegalArgumentException: 토큰이 null이거나 비어있을 경우
            return false;
        }
    }

    public String getProviderKeyFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}