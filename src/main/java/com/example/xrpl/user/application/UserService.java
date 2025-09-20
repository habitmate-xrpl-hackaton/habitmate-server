package com.example.xrpl.user.application;

public interface UserService {
    /**
     * 사용자를 팔로우하거나 언팔로우합니다.
     *
     * @param currentUserId 현재 로그인한 사용자의 ID
     * @param targetUserId  팔로우/언팔로우 대상 사용자의 ID
     */
     void toggleFollow(Long currentUserId, Long targetUserId);
}
