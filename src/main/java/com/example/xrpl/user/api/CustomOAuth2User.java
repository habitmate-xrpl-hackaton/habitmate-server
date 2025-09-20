package com.example.xrpl.user.api;

import com.example.xrpl.user.domain.Role;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    private final Role role;
    private final Long userId;
    private final String xrplAddress;
    private final String xrplSecret;
    private final Boolean isKYC;
    private final String issuerAddress;


    /**
     * Constructs a {@code DefaultOAuth2User} using the provided parameters.
     *
     * @param authorities      the authorities granted to the user
     * @param attributes       the attributes about the user
     * @param nameAttributeKey the key used to access the user's &quot;name&quot; from
     *                         the {@link #getAttributes()} map
     * @param role             the role of the user
     * @param userId           the user's ID from the database
     * @param issuerAddress    the issuer address for credentials
     */
    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            Role role, Long userId, String address, String secret, Boolean isKYC, String issuerAddress) {
        super(authorities, attributes, nameAttributeKey);
        this.role = role;
        this.userId = userId;
        this.xrplAddress = address;
        this.xrplSecret = secret;
        this.isKYC = isKYC;
        this.issuerAddress = issuerAddress;
    }

    public CustomOAuth2User(Long userId, String email, String role, String xrplAddress, String xrplSecret, Boolean isKYC, String issuerAddress) {
        super(List.of(new SimpleGrantedAuthority(role)), Map.of("email", email), "email");
        this.userId = userId;
        this.role = Role.valueOf(role.replace("ROLE_", ""));
        this.xrplAddress = xrplAddress;
        this.xrplSecret = xrplSecret;
        this.isKYC = isKYC;
        this.issuerAddress = issuerAddress;
    }

    public static CustomOAuth2User fromClaims(Claims claims) {
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);
        String providerKey = claims.getSubject();
        String walletAddress = claims.get("xrplAddress", String.class);
        String walletSecret = claims.get("xrplSecret", String.class);
        Boolean isKYC = claims.get("isKYC", Boolean.class);
        String issuerAddress = claims.get("issuerAddress", String.class);

        return new CustomOAuth2User(userId, providerKey, "ROLE_" + role, walletAddress, walletSecret, isKYC, issuerAddress);
    }
}