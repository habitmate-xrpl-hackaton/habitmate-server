package com.example.xrpl.user.application.auth;

import com.example.xrpl.user.domain.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;


@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    private final Role role;
    private final Long userId;

    /**
     * Constructs a {@code DefaultOAuth2User} using the provided parameters.
     *
     * @param authorities      the authorities granted to the user
     * @param attributes       the attributes about the user
     * @param nameAttributeKey the key used to access the user's &quot;name&quot; from
     *                         the {@link #getAttributes()} map
     * @param role             the role of the user
     * @param userId           the user's ID from the database
     */
    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            Role role, Long userId) {
        super(authorities, attributes, nameAttributeKey);
        this.role = role;
        this.userId = userId;
    }
}