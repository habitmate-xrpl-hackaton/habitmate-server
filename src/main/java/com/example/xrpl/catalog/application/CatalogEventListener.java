package com.example.xrpl.catalog.application;

import com.example.xrpl.participation.api.ChallengeParticipantCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogEventListener {

    private final CatalogCommandService catalogCommandService;

    @EventListener
    public void handleChallengeParticipantCreatedEvent(ChallengeParticipantCreatedEvent event) {
        catalogCommandService.incrementParticipantCount(event.getChallengeId());
    }
}
