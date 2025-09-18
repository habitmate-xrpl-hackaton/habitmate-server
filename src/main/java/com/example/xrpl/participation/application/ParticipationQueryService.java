package com.example.xrpl.participation.application;

import com.example.xrpl.participation.api.MyParticipationListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParticipationQueryService {

    Page<MyParticipationListDto> findMyParticipations(Long userId, Pageable pageable);
}
