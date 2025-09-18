package com.example.xrpl.catalog.application;

import com.example.xrpl.catalog.domain.event.ChallengeCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CatalogEventListener {

    @ApplicationModuleListener
    public void handleChallengeCreatedEvent(ChallengeCreatedEvent event) {
        log.info("Event: challenge_created,  Created At: {}", LocalDateTime.now());
    }
}
