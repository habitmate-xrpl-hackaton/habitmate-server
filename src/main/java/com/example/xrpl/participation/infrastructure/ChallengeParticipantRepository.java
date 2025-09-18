package com.example.xrpl.participation.infrastructure;

import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {
    Page<ChallengeParticipant> findByUserId(Long userId, Pageable pageable);
}
