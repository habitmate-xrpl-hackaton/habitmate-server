package com.example.xrpl.participation.domain.model;

import com.example.xrpl.participation.api.ChallengeParticipantCreatedEvent;
import com.example.xrpl.participation.api.ProofStatusUpdatedEvent;
import com.example.xrpl.participation.domain.ParticipationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "challenge_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeParticipant extends AbstractAggregateRoot<ChallengeParticipant> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenge_id", nullable = false, updatable = false)
    private long challengeId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "escrow_owner")
    private String escrowOwner;

    @Column(name = "offer_sequence")
    private String offerSequence;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proof> proofs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status;

    private ChallengeParticipant(long challengeId, Long userId, String escrowOwner, String offerSequence) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.escrowOwner = escrowOwner;
        this.offerSequence = offerSequence;
        this.status = ParticipationStatus.ACTIVE;
        registerEvent(new ChallengeParticipantCreatedEvent(this.challengeId));
    }

    public static ChallengeParticipant of(long challengeId, Long userId, String escrowOwner, String offerSequence) {
        return new ChallengeParticipant(challengeId, userId, escrowOwner, offerSequence);
    }

    /**
     * 챌린지 참가자의 인증(Proof)을 추가하고, 추가된 Proof 객체를 반환합니다.
     *
     * @param imageUrl    인증 이미지 URL
     * @param description 인증 설명
     * @param hashtags    해시태그 엔티티 Set
     * @return 생성 및 추가된 Proof 엔티티
     */
    public Proof addProof(String imageUrl, String description, Set<Hashtag> hashtags) {
        Proof proof = Proof.of(this, LocalDateTime.now(), false, imageUrl, description, hashtags);
        this.proofs.add(proof);
        return proof;
    }

    public void verifyProof(Long proofId, boolean isSuccess) {
        Proof proofToVerify = this.proofs.stream()
                .filter(p -> p.getId().equals(proofId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Proof not found with id: " + proofId));

        proofToVerify.verify(isSuccess);
        registerEvent(new ProofStatusUpdatedEvent(proofId, this.userId, isSuccess));
    }


    /**
     * 성공적으로 완료된 인증(Proof)의 개수를 반환합니다.
     *
     * @return 'PASSED' 상태인 인증의 총 개수
     */
    public int getPassedProofCount() {
        return (int) this.proofs.stream()
                .filter(Proof::isPassed)
                .count();
    }
}
