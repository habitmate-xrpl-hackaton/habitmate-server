package com.example.xrpl.challenge.application;

import com.example.xrpl.challenge.domain.event.ChallengeCreatedEvent;
import com.example.xrpl.challenge.domain.model.Challenge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ChallengeEventListener {

    @ApplicationModuleListener
    public void handleChallengeCreatedEvent(ChallengeCreatedEvent event) {
        log.info("Event: challenge_created,  Created At: {}", LocalDateTime.now());
    }
}
