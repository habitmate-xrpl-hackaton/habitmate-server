package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.api.ChallengeCreateRequest;
import com.example.xrpl.catalog.api.ChallengeCreateResponse;
import com.example.xrpl.catalog.api.ChallengeListDto;
import com.example.xrpl.catalog.api.ChallengeDetailDto;
import com.example.xrpl.catalog.domain.model.ChallengeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CatalogQueryService {
    ChallengeCreateResponse createChallenge(ChallengeCreateRequest request);
    Page<ChallengeListDto> findChallenges(ChallengeType type, String keyword, Pageable pageable);
    Page<ChallengeListDto> findCuratedChallenges(ChallengeType type, Pageable pageable);
    ChallengeDetailDto findChallengeDetail(Long id);
}
