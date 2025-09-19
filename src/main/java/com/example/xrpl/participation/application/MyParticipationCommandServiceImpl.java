package com.example.xrpl.participation.application;

import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MyParticipationCommandServiceImpl implements MyParticipationCommandService {

    private final ChallengeParticipantRepository participantRepository;
    private final ImageUploadService imageUploadService

    /**
     * 챌린지에 대한 인증을 추가합니다.
     *
     * @param challengeId 챌린지 ID
     * @param userId      사용자 ID
     * @param imageFile   전송 받은 이미지 파일
     * @throws IllegalArgumentException 해당 챌린지에 참여하지 않은 경우 발생
     */
    @Override
    @Transactional
    public void addProof(long challengeId, long userId, MultipartFile imageFile) {
        ChallengeParticipant participant = participantRepository.findByChallengeIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User " + userId + " is not a participant of challenge " + challengeId));

        // TODO: 인증 가능 여부(시간, 횟수 등)에 대한 비즈니스 로직 검증이 필요합니다.
        // supabase로 이미지 보낸 후, url 받아오기
        String imageUrl = imageUploadService.uploadImage(imageFile);
        participant.addProof(imageUrl);
        participantRepository.save(participant);
    }
}