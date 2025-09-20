package com.example.xrpl.activitystats.application;

import com.example.xrpl.activitystats.domain.UserActivityStats;
import com.example.xrpl.activitystats.infrastructure.UserActivityStatsRepository;
import com.example.xrpl.user.api.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final UserActivityStatsRepository userActivityStatsRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        UserActivityStats newUserActivityStats = UserActivityStats.create(event.userId());
        userActivityStatsRepository.save(newUserActivityStats);
    }
}
