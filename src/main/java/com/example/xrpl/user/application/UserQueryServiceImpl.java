package com.example.xrpl.user.application;

import com.example.xrpl.user.api.UserQueryService;
import com.example.xrpl.user.domain.User;
import com.example.xrpl.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public Map<Long, String> findWalletAddressesByUserIds(List<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getXrplAddress));
    }
}
