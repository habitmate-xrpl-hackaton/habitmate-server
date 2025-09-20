package com.example.xrpl.activitystats.application;

import com.example.xrpl.activitystats.api.UserActivityStatsDto;

public interface UserActivityStatsQueryService {
    UserActivityStatsDto findByUserId(Long userId);
}