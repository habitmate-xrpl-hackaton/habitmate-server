package com.example.xrpl.challenge.api;

import com.example.xrpl.challenge.api.dto.ChallengeCreateRequest;
import com.example.xrpl.challenge.api.dto.ChallengeCreateResponse;
import com.example.xrpl.challenge.api.dto.ChallengeListDto;
import com.example.xrpl.challenge.application.ChallengeService;
import com.example.xrpl.challenge.domain.model.ChallengeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.xrpl.challenge.api.dto.ChallengeDetailDto;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping("/challenges")
    public ResponseEntity<ChallengeCreateResponse> createChallenge(@RequestBody ChallengeCreateRequest createRequest) {
        ChallengeCreateResponse response = challengeService.createChallenge(createRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public-challenges")
    public ResponseEntity<Page<ChallengeListDto>> findPublicChallenges(
            @RequestParam(required = false) ChallengeType type,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "period.startDate") Pageable pageable
    ) {
        Page<ChallengeListDto> response = challengeService.findChallenges(type, keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/challenges/{id}")
    public ResponseEntity<ChallengeDetailDto> findChallengeDetail(@PathVariable Long id) {
        ChallengeDetailDto response = challengeService.findChallengeDetail(id);
        return ResponseEntity.ok(response);
    }
}
