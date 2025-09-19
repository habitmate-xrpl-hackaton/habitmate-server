package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.api.ChallengeCatalogInfoDto;
import com.example.xrpl.catalog.api.ChallengeDetailDto;
import com.example.xrpl.catalog.api.ChallengeQueryService;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    @Override
    public ChallengeDetailDto findChallengeById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .map(ChallengeDetailDto::from)
                .orElseThrow(NoSuchElementException::new);
    }
}
