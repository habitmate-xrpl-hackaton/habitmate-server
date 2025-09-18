package com.example.xrpl.participation.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private boolean success;

    private String proofImageUrl;

    private Proof(ChallengeParticipant participant, LocalDateTime proofAt, boolean success, String proofImageUrl) {
        this.participant = participant;
        this.proofAt = proofAt;
        this.success = success;
        this.proofImageUrl = proofImageUrl;
    }

    public static Proof of(LocalDateTime proofAt, boolean success, String proofImageUrl) {
        return new Proof(null, proofAt, success, proofImageUrl);
    }

    /**
     * 연관관계 편의 메서드. ChallengeParticipant 애그리게이트 루트에서만 호출되어야 합니다.
     * @param participant 이 Proof가 속하게 될 부모 엔티티
     */
    void setParticipant(ChallengeParticipant participant) {
        this.participant = participant;
    }
}