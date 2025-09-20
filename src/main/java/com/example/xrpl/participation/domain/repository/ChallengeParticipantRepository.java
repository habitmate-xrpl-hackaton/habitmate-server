package com.example.xrpl.participation.domain.repository;

import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {
    @Query("SELECT cp.userId FROM ChallengeParticipant cp JOIN cp.proofs p WHERE p.id = :proofId")
    Optional<Long> findUserIdByProofId(Long proofId);
}