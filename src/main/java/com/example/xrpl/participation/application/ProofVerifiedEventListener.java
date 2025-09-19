package com.example.xrpl.participation.application;

import com.example.xrpl.catalog.api.ProofVerifiedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProofVerifiedEventListener {

    private final MyParticipationCommandService myParticipationCommandService;

    @ApplicationModuleListener
    public void handleProofVerifiedEvent(ProofVerifiedEvent event) {
        myParticipationCommandService.verifyProof(event.participantId(), event.proofId(),  event.isSuccess());
    }
}
