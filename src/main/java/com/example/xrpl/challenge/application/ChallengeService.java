package com.example.xrpl.challenge.application;

import com.example.xrpl.challenge.api.dto.ChallengeCreateRequest;
import com.example.xrpl.challenge.api.dto.ChallengeCreateResponse;
import com.example.xrpl.challenge.api.dto.ChallengeListDto;
import com.example.xrpl.challenge.api.dto.ChallengeDetailDto;
import com.example.xrpl.challenge.domain.model.ChallengeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChallengeService {
    ChallengeCreateResponse createChallenge(ChallengeCreateRequest request);
    Page<ChallengeListDto> findChallenges(ChallengeType type, String keyword, Pageable pageable);
    Page<ChallengeListDto> findCuratedChallenges(ChallengeType type, Pageable pageable);
    ChallengeDetailDto findChallengeDetail(Long id);
}
