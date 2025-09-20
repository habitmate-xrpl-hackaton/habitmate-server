package com.example.xrpl.catalog.infrastructure;

import com.example.xrpl.participation.ChallengeInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ChallengeInfoProviderImpl implements ChallengeInfoProvider {

    private final ChallengeRepository challengeRepository;

    @Override
    public Optional<ChallengeInfo> findChallengeInfoById(long challengeId) {
        return challengeRepository.findById(challengeId)
                .map(challenge -> new ChallengeInfo(
                        challenge.calculateTotalProofCount()
                ));
    }
}