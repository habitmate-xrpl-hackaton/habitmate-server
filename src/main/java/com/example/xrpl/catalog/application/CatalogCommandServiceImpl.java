package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.api.CatalogCommandService;
import com.example.xrpl.catalog.domain.model.Challenge;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CatalogCommandServiceImpl implements CatalogCommandService {

    private final ChallengeRepository challengeRepository;

    @Override
    @Transactional
    public void endChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));
        challenge.endChallenge();
        challengeRepository.save(challenge);
    }
}
