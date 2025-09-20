package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.api.ChallengeDetailDto;
import com.example.xrpl.catalog.api.CatalogQueryService;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeQueryServiceImpl implements CatalogQueryService {

    private final ChallengeRepository challengeRepository;

    @Override
    public List<ChallengeDetailDto> findChallengesByIds(List<Long> challengeIds) {
        return challengeRepository.findAllById(challengeIds).stream()
                .map(ChallengeDetailDto::from)
                .toList();
    }

    @Override
    public ChallengeDetailDto findChallengeById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .map(ChallengeDetailDto::from)
                .orElseThrow(NoSuchElementException::new);
    }
}
