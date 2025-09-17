package com.example.xrpl.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String providerKey;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private User(String email, String providerKey, Role role) {
        this.email = email;
        this.providerKey = providerKey;
        this.role = role;
    }

    /**
     * OAuth2 신규 사용자를 위한 User 애그리게이트를 생성합니다.
     * 생성의 책임은 User 애그리게이트 자신에게 있습니다.
     *
     * @param email       사용자 이메일
     * @param providerKey OAuth 제공자의 고유 키 (예: Google의 'sub')
     * @return 새로 생성된 User 객체
     */
    public static User createNewUser(String email, String providerKey) {
        return new User(email, providerKey, Role.USER);
    }
}