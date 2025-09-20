package com.example.xrpl.security;

import com.example.xrpl.user.api.CustomOAuth2User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        CustomOAuth2User principal = new CustomOAuth2User(customUser.userId(), customUser.email(), "ROLE_USER", customUser.xrplAddress(), customUser.xrplSecret());
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
