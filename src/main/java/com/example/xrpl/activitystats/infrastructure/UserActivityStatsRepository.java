package com.example.xrpl.activitystats.infrastructure;

import com.example.xrpl.activitystats.domain.UserActivityStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserActivityStatsRepository extends JpaRepository<UserActivityStats, Long> {
    Optional<UserActivityStats> findByUserId(Long userId);
}
