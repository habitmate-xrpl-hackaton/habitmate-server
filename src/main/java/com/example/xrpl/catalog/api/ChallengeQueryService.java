package com.example.xrpl.catalog.api;

import java.util.List;

public interface ChallengeQueryService {
    List<ChallengeCatalogInfoDto> findChallengesByIds(List<Long> challengeIds);
}
