package com.example.xrpl.participation.domain.model;

import com.example.xrpl.participation.api.ProofAddedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;
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
     * 이 메서드는 애그리게이트의 상태를 변경하는 유일한 진입점 역할을 합니다.
     * 내부적으로 Proof를 생성하고, 양방향 연관관계를 설정하며, 도메인 이벤트를 발행합니다.
     * @param imageUrl 인증 이미지 URL
     */
    public void addProof(String imageUrl) {
        Proof proof = Proof.of(this, LocalDateTime.now(), true, imageUrl);
        this.proofs.add(proof);
        registerEvent(new ProofAddedEvent(this.id));
    }
}
