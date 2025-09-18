package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.api.ChallengeCreateRequest;
import com.example.xrpl.catalog.api.ChallengeCreateResponse;
import com.example.xrpl.catalog.api.ChallengeListDto;
import com.example.xrpl.catalog.domain.model.Challenge;
import com.example.xrpl.catalog.domain.model.ChallengeType;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import com.example.xrpl.catalog.application.curation.LatestChallengesCurationStrategy;
import com.example.xrpl.catalog.api.ChallengeDetailDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CatalogQueryServiceImpl implements CatalogQueryService {

    private final ChallengeRepository challengeRepository;
    private final LatestChallengesCurationStrategy latestChallengesCurationStrategy;

    @Override
    @Transactional
    public ChallengeCreateResponse createChallenge(ChallengeCreateRequest request) {
        Challenge challenge = Challenge.of(request);
        challengeRepository.save(challenge);

        return new ChallengeCreateResponse(challenge.getId(), challenge.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChallengeListDto> findChallenges(ChallengeType type, String keyword, Pageable pageable) {
        Specification<Challenge> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likeKeyword),
                        cb.like(cb.lower(root.get("description")), likeKeyword)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return challengeRepository.findAll(spec, pageable)
                .map(ChallengeListDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChallengeListDto> findCuratedChallenges(ChallengeType type, Pageable pageable) {
        return latestChallengesCurationStrategy.curate(type, pageable)
                .map(ChallengeListDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ChallengeDetailDto findChallengeDetail(Long id) {
        return challengeRepository.findById(id)
                .map(ChallengeDetailDto::from)
                .orElseThrow(() -> new NoSuchElementException("Challenge not found with id: " + id));
    }
}
