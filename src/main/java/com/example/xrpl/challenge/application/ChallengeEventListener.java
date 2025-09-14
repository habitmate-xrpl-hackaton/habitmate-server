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
        Challenge challenge = event.challenge();
        // 트랜잭션 커밋 후 리스너가 동작하므로, challenge.getId()는 항상 유효합니다.
        log.info("Event: challenge_created, Challenge ID: {}, Created At: {}", challenge.getId(), LocalDateTime.now());
    }
}
