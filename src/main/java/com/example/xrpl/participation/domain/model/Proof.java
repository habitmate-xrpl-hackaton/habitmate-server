package com.example.xrpl.participation.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "proofs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Proof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ChallengeParticipant participant;

    @Column(nullable = false)
    private LocalDateTime proofAt;

    @Column(nullable = false)
    private boolean success; // 초기값은 false 또는 인증 대기 상태를 나타내는 별도 Enum으로 관리 가능

    @Column(nullable = false)
    private String proofImageUrl;

    @Column
    private String description;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "proof_hashtags",
            joinColumns = @JoinColumn(name = "proof_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags = new HashSet<>();

    private Proof(ChallengeParticipant participant, LocalDateTime proofAt, boolean success, String proofImageUrl, String description, Set<Hashtag> hashtags) {
        this.participant = participant;
        this.proofAt = proofAt;
        this.success = success;
        this.proofImageUrl = proofImageUrl;
        this.description = description;
        this.hashtags = hashtags;
    }

    static Proof of(ChallengeParticipant participant, LocalDateTime proofAt, boolean success, String proofImageUrl, String description, Set<Hashtag> hashtags) {
        return new Proof(participant, proofAt, success, proofImageUrl, description, hashtags);
    }

    void verify(boolean isSuccess) {
        this.success = isSuccess;
    }
}