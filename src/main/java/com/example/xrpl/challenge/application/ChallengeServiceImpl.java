package com.example.xrpl.challenge.application;

import com.example.xrpl.challenge.api.dto.ChallengeCreateRequest;
import com.example.xrpl.challenge.api.dto.ChallengeCreateResponse;
import com.example.xrpl.challenge.domain.model.Challenge;
import com.example.xrpl.challenge.infrastructure.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;

    @Override
    @Transactional
    public ChallengeCreateResponse createChallenge(ChallengeCreateRequest request) {
        Challenge challenge = Challenge.of(request);
        challengeRepository.save(challenge);

        return new ChallengeCreateResponse(challenge.getId(), challenge.getStatus());
    }
}
