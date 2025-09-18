package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.api.FeeDto;
import com.example.xrpl.catalog.api.FeeQueryService;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FeeQueryServiceImpl implements FeeQueryService {

    private final ChallengeRepository challengeRepository;

    @Override
    public FeeDto findFee(long challengerId) {
        return challengeRepository.findById(challengerId)
                .map(FeeDto::from)
                .orElseThrow(NoSuchElementException::new);
    }
}
