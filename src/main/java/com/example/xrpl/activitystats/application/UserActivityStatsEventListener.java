package com.example.xrpl.activitystats.application;

import com.example.xrpl.activitystats.domain.UserActivityStats;
import com.example.xrpl.activitystats.infrastructure.UserActivityStatsRepository;
import com.example.xrpl.participation.api.ProofStatusUpdatedEvent; 
import com.example.xrpl.user.api.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityStatsEventListener {

    private final UserActivityStatsRepository userActivityStatsRepository;

    @Async
    @TransactionalEventListener()
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for userId: {}", event.userId());
        if (userActivityStatsRepository.findByUserId(event.userId()).isEmpty()) {
            UserActivityStats newUserActivityStats = UserActivityStats.create(event.userId());
            userActivityStatsRepository.save(newUserActivityStats);
            log.info("UserActivityStats created for userId: {}", event.userId());
        }
    }

    @ApplicationModuleListener
    public void handleProofStatusUpdatedEvent(ProofStatusUpdatedEvent event) {
        log.info("Received ProofStatusUpdatedEvent for proofId: {}, isSuccess: {}", event.proofId(), event.isSuccess());
        UserActivityStats stats = userActivityStatsRepository.findByUserId(event.userId())
                .orElseThrow(() -> new IllegalStateException("Cannot find UserActivityStats for userId: " + event.userId()));
        stats.recordProofVerification(event.isSuccess());
    }
}