package com.example.xrpl.challenge.api;

import com.example.xrpl.challenge.api.dto.ChallengeListDto;
import com.example.xrpl.challenge.application.ChallengeService;
import com.example.xrpl.challenge.domain.model.ChallengeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CuratedChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/curated-challenges")
    public ResponseEntity<Page<ChallengeListDto>> findCuratedChallenges(
            @RequestParam ChallengeType type,
            @PageableDefault(size = 10, sort = "period.startDate") Pageable pageable
    ) {
        if (type != ChallengeType.GROUP && type != ChallengeType.BRAND) {
            return ResponseEntity.badRequest().build();
        }
        Page<ChallengeListDto> response = challengeService.findCuratedChallenges(type, pageable);
        return ResponseEntity.ok(response);
    }
}