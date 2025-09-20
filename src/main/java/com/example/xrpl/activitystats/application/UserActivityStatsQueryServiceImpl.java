package com.example.xrpl.activitystats.application;

import com.example.xrpl.activitystats.api.UserActivityStatsDto;
import com.example.xrpl.activitystats.domain.UserActivityStats;
import com.example.xrpl.activitystats.infrastructure.UserActivityStatsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityStatsQueryServiceImpl implements UserActivityStatsQueryService {

    private final UserActivityStatsRepository userActivityStatsRepository;

    @Override
    public UserActivityStatsDto findByUserId(Long userId) {
        UserActivityStats stats = userActivityStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User activity stats not found for user id: " + userId));
        return UserActivityStatsDto.from(stats);
    }
}