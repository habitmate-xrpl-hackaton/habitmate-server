package com.example.xrpl.user.api;

import com.example.xrpl.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String email;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.email = email;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        // 현재는 google만 처리. 향후 naver, kakao 등 추가 시 분기 처리 필요
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public String getProviderKey() {
        return (String) attributes.get(nameAttributeKey);
    }

    public User toEntity() {
        return User.createNewUser(email, getProviderKey());
    }
}