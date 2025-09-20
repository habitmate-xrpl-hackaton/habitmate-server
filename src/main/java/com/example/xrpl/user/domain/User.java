package com.example.xrpl.user.domain;

import com.example.xrpl.user.api.UserCreatedEvent;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AbstractAggregateRoot<User> {

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

    @Column(nullable = false)
    private String xrplAddress;

    @Column(nullable = false)
    private String xrplSecret;

    @ManyToMany(cascade = {PERSIST, REMOVE})
    @JoinTable(name = "follow",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id"))
    private final Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following", cascade = {PERSIST, REMOVE})
    private final Set<User> followers = new HashSet<>();

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
        User user = new User(email, providerKey, Role.USER);
        user.registerEvent(new UserCreatedEvent(user.getId()));
        return user;
    }

    /**
     * 사용자를 팔로우하거나 언팔로우합니다.
     * 이미 팔로우 중이면 언팔로우하고, 그렇지 않으면 팔로우합니다.
     *
     * @param userToFollow 팔로우/언팔로우할 사용자
     */
    public void toggleFollow(User userToFollow) {
        if (this.following.contains(userToFollow)) {
            unfollow(userToFollow);
        } else {
            follow(userToFollow);
        }
    }

    private void follow(User user) {
        this.following.add(user);
        user.getFollowers().add(this);
    }

    private void unfollow(User user) {
        this.following.remove(user);
        user.getFollowers().remove(this);
    }
}