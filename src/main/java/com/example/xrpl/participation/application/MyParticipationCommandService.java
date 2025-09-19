package com.example.xrpl.participation.application;

import org.springframework.web.multipart.MultipartFile;

public interface MyParticipationCommandService {
    void addProof(long challengeId, long userId, MultipartFile imageFile);
}
