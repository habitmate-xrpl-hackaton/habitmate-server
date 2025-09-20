package com.example.xrpl.activitystats.application;

import com.example.xrpl.activitystats.domain.UserActivityStats;
import com.example.xrpl.activitystats.infrastructure.UserActivityStatsRepository;
import com.example.xrpl.user.api.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final UserActivityStatsRepository userActivityStatsRepository;

    @ApplicationModuleListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        UserActivityStats newUserActivityStats = UserActivityStats.create(event.userId());
        userActivityStatsRepository.save(newUserActivityStats);
    }
}
