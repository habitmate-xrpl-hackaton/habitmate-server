package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.api.ChallengeCatalogInfoDto;
import com.example.xrpl.catalog.api.ChallengeQueryService;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeQueryServiceImpl implements ChallengeQueryService {

    private final ChallengeRepository challengeRepository;

    @Override
    public List<ChallengeCatalogInfoDto> findChallengesByIds(List<Long> challengeIds) {
        return challengeRepository.findAllById(challengeIds).stream()
                .map(ChallengeCatalogInfoDto::from)
                .toList();
    }
}
