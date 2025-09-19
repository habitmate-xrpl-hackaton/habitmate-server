package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.api.ChallengeCommandService;
import com.example.xrpl.catalog.domain.model.Challenge;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ChallengeCommandServiceImpl implements ChallengeCommandService {

    private final ChallengeRepository challengeRepository;

    @Override
    @Transactional
    public void increaseParticipantCount(long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found with id: " + challengeId));
        challenge.participate();
    }
}
