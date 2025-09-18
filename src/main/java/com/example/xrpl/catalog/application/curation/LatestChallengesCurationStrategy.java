package com.example.xrpl.catalog.application.curation;

import com.example.xrpl.catalog.domain.model.Challenge;
import com.example.xrpl.catalog.domain.model.ChallengeType;
import com.example.xrpl.catalog.infrastructure.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component("latestChallengesCurationStrategy")
@RequiredArgsConstructor
public class LatestChallengesCurationStrategy implements ChallengeCurationStrategy {

    private final ChallengeRepository challengeRepository;

    @Override
    public Page<Challenge> curate(ChallengeType type, Pageable pageable) {
        Specification<Challenge> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort latestSort = Sort.by(Sort.Direction.DESC, "period.startDate");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), latestSort);

        return challengeRepository.findAll(spec, sortedPageable);
    }
}