package com.example.xrpl.participation.application;

import com.example.xrpl.catalog.api.ChallengeCatalogInfoDto;
import com.example.xrpl.catalog.api.CatalogQueryService;
import com.example.xrpl.catalog.api.ChallengeDetailDto;
import com.example.xrpl.participation.api.MyParticipationListDto;
import com.example.xrpl.participation.domain.model.ChallengeParticipant;
import com.example.xrpl.participation.infrastructure.ChallengeParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyParticipationQueryServiceImpl implements MyParticipationQueryService {

    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final CatalogQueryService challengeQueryService;

    @Override
    public Page<MyParticipationListDto> findMyParticipations(Long userId, Pageable pageable) {
        // 1. Participation 모듈의 DB에서 사용자가 참여중인 챌린지 목록을 페이징하여 조회
        Page<ChallengeParticipant> participantsPage = challengeParticipantRepository.findByUserId(userId, pageable);
        List<Long> challengeIds = participantsPage.getContent().stream()
                .map(ChallengeParticipant::getChallengeId)
                .toList();

        // 2. Catalog 모듈의 API를 통해 필요한 챌린지 정보들을 한번에 조회
        Map<Long, ChallengeDetailDto> challengeInfoMap = challengeQueryService.findChallengesByIds(challengeIds).stream()
                .collect(Collectors.toMap(ChallengeDetailDto::id, Function.identity()));

        // 3. 두 데이터를 조합하여 최종 DTO 리스트 생성
        List<MyParticipationListDto> dtoList = participantsPage.getContent().stream()
                .map(participant -> {
                    ChallengeDetailDto challengeInfo = challengeInfoMap.get(participant.getChallengeId());
                    if (challengeInfo == null) {
                        throw new IllegalStateException("Challenge information not found for ID: " + participant.getChallengeId());
                    }

                    long totalProofCount = calculateTotalProofCount(challengeInfo);
                    long totalParticipatingCount = participant.getProofs().size();

                    return new MyParticipationListDto(
                            challengeInfo.id(),
                            challengeInfo.title(),
                            totalParticipatingCount,
                            totalProofCount,
                            challengeInfo.entryFee(),
                            challengeInfo.serviceFee(),
                            challengeInfo.startDate(),
                            challengeInfo.endDate()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        // 4. 최종 결과를 Page 객체로 만들어 반환
        return new PageImpl<>(dtoList, pageable, participantsPage.getTotalElements());
    }

    private long calculateTotalProofCount(ChallengeDetailDto challengeInfo) {
        if (challengeInfo.startDate() == null || challengeInfo.endDate() == null) {
            return 0;
        }
        long totalDays = ChronoUnit.DAYS.between(challengeInfo.startDate(), challengeInfo.endDate()) + 1;
        long baseWeeks = totalDays / 7;
        long extraDays = totalDays % 7;
        int timesPerWeek = challengeInfo.frequency();

        return baseWeeks * timesPerWeek + Math.min(extraDays, timesPerWeek);
    }
}
