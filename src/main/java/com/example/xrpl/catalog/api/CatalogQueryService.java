package com.example.xrpl.catalog.api;

import java.util.List;

public interface CatalogQueryService {
    List<ChallengeDetailDto> findChallengesByIds(List<Long> challengeIds);
    ChallengeDetailDto findChallengeById(Long challengeId);
}
