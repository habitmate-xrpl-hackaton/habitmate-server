package com.example.xrpl.catalog.api;

public interface CatalogCommandService {
    void endChallenge(Long challengeId);

    void incrementParticipantCount(long challengeId);
}
