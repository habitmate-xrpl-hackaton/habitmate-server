package com.example.xrpl.participation.application;

import com.example.xrpl.participation.api.ProofAddedEvent;
import com.example.xrpl.participation.api.ProofCreateRequest;
import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import com.example.xrpl.participation.domain.model.Hashtag;
import com.example.xrpl.participation.domain.model.Proof;
import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
import com.example.xrpl.participation.infrastructure.HashtagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyParticipationCommandServiceImpl implements MyParticipationCommandService {

    private final ChallengeParticipantRepository participantRepository;
    private final HashtagRepository hashtagRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void addProof(long challengeId, long userId, ProofCreateRequest request) {
        ChallengeParticipant participant = participantRepository.findByChallengeIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User " + userId + " is not a participant of challenge " + challengeId));

        final Set<String> requestedHashtagNames = request.hashtags() == null ? Collections.emptySet() : request.hashtags();
        Set<Hashtag> hashtagsForProof = new HashSet<>();

        if (!requestedHashtagNames.isEmpty()) {
            final Set<Hashtag> existingHashtags = new HashSet<>(hashtagRepository.findByNameIn(requestedHashtagNames));
            final Set<String> existingNames = existingHashtags.stream().map(Hashtag::getName).collect(Collectors.toSet());

            final Set<Hashtag> newHashtags = requestedHashtagNames.stream()
                    .filter(name -> !existingNames.contains(name))
                    .map(Hashtag::of)
                    .collect(Collectors.toSet());

            hashtagsForProof.addAll(existingHashtags);
            hashtagsForProof.addAll(newHashtags);
        }

        // 1. 도메인 메서드를 호출하여 Proof 객체를 생성하고 컬렉션에 추가합니다.
        Proof newProof = participant.addProof(request.imageUrl(), request.description(), hashtagsForProof);

        // 2. 애그리게이트 루트를 저장합니다. JPA가 자식 엔티티인 Proof도 함께 저장(persist)합니다.
        participantRepository.save(participant);

        // 3. save()가 호출된 후, newProof 객체에는 데이터베이스에서 생성된 ID가 할당됩니다.
        //    이제 ID를 포함하여 이벤트를 발행할 수 있습니다.
        eventPublisher.publishEvent(new ProofAddedEvent(
                participant.getChallengeId(),
                participant.getId(),
                newProof.getId(),
                newProof.getProofImageUrl()
        ));
    }

    @Override
    @Transactional
    public void verifyProof(Long participantId, Long proofId, boolean isSuccess) {
        ChallengeParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("ChallengeParticipant not found with id: " + participantId));

        participant.verifyProof(proofId, isSuccess);
    }
}
