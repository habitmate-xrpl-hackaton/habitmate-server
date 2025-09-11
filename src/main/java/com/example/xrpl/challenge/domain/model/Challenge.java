package com.example.xrpl.challenge.domain.model;

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
    private Fee fee;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "challenge_rules", joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "rule_description")
    @OrderColumn(name = "rule_order")
    private List<String> rules = new ArrayList<>();
}
