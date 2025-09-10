package com.example.xrpl.challenge.application;

import com.example.xrpl.challenge.api.dto.ChallengeCreateRequest;
import com.example.xrpl.challenge.api.dto.ChallengeCreateResponse;

public interface ChallengeService {
    ChallengeCreateResponse createChallenge(ChallengeCreateRequest request);
}
