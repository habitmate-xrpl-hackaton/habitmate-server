package com.example.xrpl.catalog.application.curation;

import com.example.xrpl.catalog.domain.model.Challenge;
import com.example.xrpl.catalog.domain.model.ChallengeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChallengeCurationStrategy {
    Page<Challenge> curate(ChallengeType type, Pageable pageable);
}