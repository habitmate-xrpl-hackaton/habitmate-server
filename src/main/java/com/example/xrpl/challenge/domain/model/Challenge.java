package com.example.xrpl.challenge.domain.model;

import com.example.xrpl.challenge.api.dto.ChallengeCreateRequest;
import com.example.xrpl.challenge.domain.event.ChallengeCreatedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "challenges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends AbstractAggregateRoot<Challenge> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private ChallengeStatus status;

    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ChallengeType type;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @OneToMany(mappedBy = "challenge",
            cascade =  CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<ChallengeParticipant> participants = new HashSet<>();

    @Embedded
    private Period period;

    @Embedded
    private VerificationRule verificationRule;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "currency", column = @Column(name = "entry_fee_currency")),
            @AttributeOverride(name = "amount", column = @Column(name = "entry_fee_amount"))
    })
    private Fee entryFee;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "currency", column = @Column(name = "service_fee_currency")),
            @AttributeOverride(name = "amount", column = @Column(name = "service_fee_amount"))
    })
    private Fee serviceFee; // serviceFee는 null일 수 있음

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "challenge_rules", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "rule_description")
    @OrderColumn(name = "rule_order")
    private List<String> rules = new ArrayList<>();

    private int maxParticipants;

    private Challenge(String title, String description, ChallengeType type, Category category, Difficulty difficulty, Period period, VerificationRule verificationRule, Fee entryfee, Fee serviceFee, List<String> rules, int maxParticipants) {
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
    }

    private static Challenge createSoloChallenge(ChallengeCreateRequest request) {
        return new Challenge(request.title(),request.description(), ChallengeType.SOLO, request.category(),request.difficulty(), new Period(request.startDate(), request.endDate()), new VerificationRule(request.proofType(), request.frequency()), request.entryFee(), null, request.rules(), 1);
    }

    private static Challenge createGroupChallenge(ChallengeCreateRequest request) {
        return new Challenge(request.title(),request.description(), ChallengeType.GROUP, request.category(),request.difficulty(), new Period(request.startDate(), request.endDate()), new VerificationRule(request.proofType(), request.frequency()),request.entryFee(),request.serviceFee(), request.rules(), request.maxParticipants());
    }

    public static Challenge of(ChallengeCreateRequest request) {
        checkValid(request);

        Challenge challenge;

        if (request.type() == ChallengeType.SOLO) {
            challenge = Challenge.createSoloChallenge(request);
        } else if (request.type() == ChallengeType.GROUP) {
            challenge = Challenge.createGroupChallenge(request);
        } else {
            throw new IllegalStateException("Unexpected value: " + request.type());
        }

        challenge.registerEvent(new ChallengeCreatedEvent(challenge));

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
}
