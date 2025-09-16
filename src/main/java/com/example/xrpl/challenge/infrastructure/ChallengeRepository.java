package com.example.xrpl.challenge.infrastructure;

import com.example.xrpl.challenge.domain.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, JpaSpecificationExecutor<Challenge> {
}
