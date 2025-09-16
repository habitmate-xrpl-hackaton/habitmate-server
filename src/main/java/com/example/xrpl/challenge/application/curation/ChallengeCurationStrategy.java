package com.example.xrpl.challenge.application.curation;

import com.example.xrpl.challenge.domain.model.Challenge;
import com.example.xrpl.challenge.domain.model.ChallengeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChallengeCurationStrategy {
    Page<Challenge> curate(ChallengeType type, Pageable pageable);
}