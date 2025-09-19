package com.example.xrpl.participation.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record ProofCreateRequest(
        @Schema(description = "업로드된 인증 이미지 URL", example = "https://example.com/image.jpg")
        @NotBlank(message = "이미지 URL은 필수입니다.")
        String imageUrl,
        @Schema(description = "인증 설명", example = "오늘도 운동 완료!")
        String description,
        @Schema(description = "해시태그 목록", example = "[\"오운완\", \"챌린지\"]")
        Set<String> hashtags
) {}