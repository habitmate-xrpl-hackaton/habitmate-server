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

    @Column
    private Boolean isKYC;

    @Column
    private String issuerAddress;

    @Column
    private String credentialType;

    @ManyToMany(cascade = {PERSIST, REMOVE})
    @JoinTable(name = "follow",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id"))
    private final Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following", cascade = {PERSIST, REMOVE})
    private final Set<User> followers = new HashSet<>();

    private User(String email, String providerKey, Role role, String xrplAddress, String xrplSecret, boolean isKYC) {
        this.email = email;
        this.providerKey = providerKey;
        this.role = role;
        this.xrplAddress = xrplAddress;
        this.xrplSecret = xrplSecret;
        this.isKYC = isKYC;
    }

    public static User createNewUser(String email, String providerKey, String xrplAddress, String xrplSecret) {
        User user = new User(email, providerKey, Role.USER, xrplAddress, xrplSecret, false);
        user.registerEvent(new UserCreatedEvent(user.getId()));
        return user;
    }

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
    
    public void updateKYC() {
        this.isKYC = true;
    }

    public void updateCredentialInfo(String issuerAddress, String credentialType) {
        this.issuerAddress = issuerAddress;
        this.credentialType = credentialType;
    }
}