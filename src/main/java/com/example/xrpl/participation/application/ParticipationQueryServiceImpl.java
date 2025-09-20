package com.example.xrpl.participation.application;

import com.example.xrpl.participation.api.ParticipationQueryService;
import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ParticipationQueryServiceImpl implements ParticipationQueryService {

    private final ChallengeParticipantRepository participantRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ParticipantInfo> findParticipantsByChallengeId(long challengeId) {
        return participantRepository.findByChallengeId(challengeId).stream()
                .map(p -> new ParticipantInfo(
                        p.getUserId(),
                        p.getEscrowOwner(),
                        p.getOfferSequence(),
                        p.getPassedProofCount()
                ))
                .collect(Collectors.toList());
    }
}
