package com.example.xrpl.participation.domain.model;

import com.example.xrpl.participation.domain.model.event.ProofAddedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenge_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"challengeId", "userId"})
public class ChallengeParticipant extends AbstractAggregateRoot<ChallengeParticipant> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenge_id", nullable = false, updatable = false)
    private long challengeId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proof> proofs = new ArrayList<>();

    private ChallengeParticipant(long challengeId, Long userId) {
        this.challengeId = challengeId;
        this.userId = userId;
    }

    public static ChallengeParticipant of(long challengeId, Long userId) {
        return new ChallengeParticipant(challengeId, userId);
    }

    /**
     * 챌린지 참가자의 인증(Proof)을 추가합니다.
     * 양방향 연관관계를 설정하고, 인증 추가 이벤트를 발행합니다.
     * @param proof 추가할 인증 정보
     */
    public void addProof(Proof proof) {
        this.proofs.add(proof);
        proof.setParticipant(this);
        registerEvent(new ProofAddedEvent(this.id));
    }
}
