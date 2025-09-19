package com.example.xrpl.participation.application;

import com.example.xrpl.catalog.api.ChallengeCommandService;
import com.example.xrpl.participation.api.ProofCreateRequest;
import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import com.example.xrpl.participation.domain.model.Hashtag;
import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
import com.example.xrpl.participation.infrastructure.HashtagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MyParticipationCommandServiceImpl implements MyParticipationCommandService {

    private final ChallengeParticipantRepository participantRepository;
    private final HashtagRepository hashtagRepository;

    @Override
    @Transactional
    public void addProof(long challengeId, long userId, ProofCreateRequest request) {
        ChallengeParticipant participant = participantRepository.findByChallengeIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User " + userId + " is not a participant of challenge " + challengeId));

        final Set<String> requestedNames = request.hashtags() == null ? Collections.emptySet() : request.hashtags();
        if (requestedNames.isEmpty()) {
            participant.addProof(request.imageUrl(), request.description(), Collections.emptySet());
            participantRepository.save(participant);
            return;
        }

        final Set<Hashtag> existingHashtags = new HashSet<>(hashtagRepository.findByNameIn(requestedNames));
        final Set<String> existingNames = existingHashtags.stream().map(Hashtag::getName).collect(Collectors.toSet());

        final Set<Hashtag> newHashtags = requestedNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(Hashtag::of)
                .collect(Collectors.toSet());

        participant.addProof(request.imageUrl(), request.description(), Stream.concat(existingHashtags.stream(), newHashtags.stream()).collect(Collectors.toSet()));
        participantRepository.save(participant);
    }

    @Override
    @Transactional
    public void verifyProof(Long participantId, Long proofId, boolean isSuccess) {
        ChallengeParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("ChallengeParticipant not found with id: " + participantId));
        
        participant.verifyProof(proofId, isSuccess);
    }
}
