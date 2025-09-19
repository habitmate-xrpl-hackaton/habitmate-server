package com.example.xrpl.participation.infrastructure;

import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {
    Page<ChallengeParticipant> findByUserId(Long userId, Pageable pageable);
    Optional<ChallengeParticipant> findByChallengeIdAndUserId(long challengeId, long userId);

    @EntityGraph(attributePaths = {"proofs"})
    Optional<ChallengeParticipant> findWithProofsById(Long id);
}
