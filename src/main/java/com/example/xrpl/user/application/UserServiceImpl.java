package com.example.xrpl.user.application;

import com.example.xrpl.user.domain.User;
import com.example.xrpl.user.infrastructure.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void toggleFollow(Long currentUserId, Long targetUserId) {
        // 1. 자기 자신을 팔로우하는 것을 방지
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        // 2. User 엔티티 조회 (존재하지 않을 경우 예외 발생)
        User currentUser = findUserById(currentUserId);
        User targetUser = findUserById(targetUserId);

        // 3. 도메인 객체에 비즈니스 로직 위임
        currentUser.toggleFollow(targetUser);

        // 4. @Transactional에 의해 메서드 종료 시 변경 감지(Dirty Checking)로 자동 업데이트
    }

    /**
     * ID로 사용자를 찾아 반환합니다. 사용자가 없을 경우 EntityNotFoundException을 발생시킵니다.
     *
     * @param userId 찾을 사용자의 ID
     * @return 찾아낸 User 객체
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    @Override
    @Transactional
    public void updateKYC(Long userId) {
        User user = findUserById(userId);
        user.updateKYC();
    }
}
