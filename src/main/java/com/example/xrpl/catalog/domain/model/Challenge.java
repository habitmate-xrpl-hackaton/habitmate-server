package com.example.xrpl.catalog.domain.model;

import com.example.xrpl.catalog.api.ChallengeCompletedEvent;
import com.example.xrpl.catalog.api.ChallengeCreateRequest;
import com.example.xrpl.catalog.domain.event.ChallengeCreatedEvent;

import jakarta.persistence.*;
import jdk.jfr.Frequency;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "challenges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends AbstractAggregateRoot<Challenge> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private ChallengeType type;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Embedded
    private Period period;

    @Embedded
    private VerificationRule verificationRule;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "currency", column = @Column(name = "entry_fee_currency", length = 500)),
            @AttributeOverride(name = "amount", column = @Column(name = "entry_fee_amount", precision = 38, scale = 10))
    })
    private Fee entryFee;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "currency", column = @Column(name = "service_fee_currency", length = 500)),
            @AttributeOverride(name = "amount", column = @Column(name = "service_fee_amount", precision = 39, scale = 10))
    })
    private Fee serviceFee; // serviceFee는 null일 수 있음

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "challenge_rules", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "rule_description", length = 500)
    @OrderColumn(name = "rule_order")
    private List<String> rules = new ArrayList<>();

    private int maxParticipants;

    private int currentParticipantsCount;

    private Challenge(String title, String description, ChallengeType type, Category category, Difficulty difficulty, Period period, VerificationRule verificationRule, Fee entryfee, Fee serviceFee, List<String> rules, int maxParticipants, int currentParticipantsCount) {
        this.status = ChallengeStatus.RECRUITING;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.difficulty = difficulty;
        this.period = period;
        this.verificationRule = verificationRule;
        this.entryFee = entryfee;
        this.serviceFee = serviceFee;
        this.rules = rules;
        this.maxParticipants = maxParticipants;
        this.currentParticipantsCount = currentParticipantsCount;
    }

    private static Challenge createSoloChallenge(ChallengeCreateRequest request) {
        return new Challenge(request.title(), request.description(), ChallengeType.SOLO, request.category(), request.difficulty(), new Period(request.startDate(), request.endDate()), new VerificationRule(request.proofType(), request.frequency()), request.entryFee(), new Fee("XRP", new BigDecimal("0.0")), request.rules(), 1, 1);
    }

    private static Challenge createGroupChallenge(ChallengeCreateRequest request) {
        return new Challenge(request.title(), request.description(), ChallengeType.GROUP, request.category(), request.difficulty(), new Period(request.startDate(), request.endDate()), new VerificationRule(request.proofType(), request.frequency()), request.entryFee(), request.serviceFee(), request.rules(), request.maxParticipants(), 1);
    }

    private static Challenge createBrandChallenge(ChallengeCreateRequest request) {
        return new Challenge(request.title(), request.description(), ChallengeType.BRAND, request.category(), request.difficulty(), new Period(request.startDate(), request.endDate()), new VerificationRule(request.proofType(), request.frequency()), request.entryFee(), request.serviceFee(), request.rules(), request.maxParticipants(), 1);
    }

    public static Challenge of(ChallengeCreateRequest request) {
        checkValid(request);

        Challenge challenge;

        if (request.type() == ChallengeType.SOLO) {
            challenge = Challenge.createSoloChallenge(request);
        } else if (request.type() == ChallengeType.GROUP) {
            challenge = Challenge.createGroupChallenge(request);
        } else if (request.type() == ChallengeType.BRAND) {
            challenge = Challenge.createBrandChallenge(request);
        } else {
            throw new IllegalStateException("Unexpected value: " + request.type());
        }

        challenge.registerEvent(new ChallengeCreatedEvent());

        return challenge;
    }

    private static void checkValid(ChallengeCreateRequest request) {
        if (request.type() == null) {
            throw new IllegalArgumentException("ChallengeType cannot be null");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (request.description() == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (request.category() == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (request.difficulty() == null) {
            throw new IllegalArgumentException("Difficulty cannot be null");
        }
        if (request.startDate() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (request.endDate() == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (request.frequency() == null) {
            throw new IllegalArgumentException("Proof frequency cannot be null");
        }
        if (request.entryFee() == null) {
            throw new IllegalArgumentException("Fee cannot be null");
        }
        if (request.proofType() == null) {
            throw new IllegalArgumentException("Proof type cannot be null");
        }
        if (request.rules() == null || request.rules().isEmpty()) {
            throw new IllegalArgumentException("Rules cannot be null or empty");
        }
    }

    public void participate() {
        if (this.status != ChallengeStatus.RECRUITING || this.currentParticipantsCount >= this.maxParticipants) {
            throw new IllegalStateException("Challenge is not recruiting or is already full.");
        }
        this.currentParticipantsCount++;
    }

    /**
     * 챌린지를 종료 상태로 변경하고, 관련 이벤트를 발행합니다.
     */
    public void endChallenge() {
        if (this.status == ChallengeStatus.COMPLETED) {
            throw new IllegalStateException("Challenge has already ended.");
        }
        this.status = ChallengeStatus.COMPLETED;

        // 챌린지 종료 이벤트 발행
        registerEvent(new ChallengeCompletedEvent(this.id));
    }


    public int calculateTotalProofCount() {
        long durationDays = java.time.temporal.ChronoUnit.DAYS.between(this.period.startDate(), this.period.endDate()) + 1;
        ProofFrequency frequency = this.verificationRule.frequency();
        return frequency.calculateTotalCount(durationDays);
    }
}
