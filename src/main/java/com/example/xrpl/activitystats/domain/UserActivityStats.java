package com.example.xrpl.activitystats.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor; 

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "user_activity_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivityStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int consecutiveSuccessDays = 0;

    @Column(nullable = false)
    private int maxConsecutiveSuccessDays = 0;

    @Column(nullable = false)
    private long totalProofCount = 0L;

    @Column(nullable = false)
    private long successProofCount = 0L;

    @Column
    private LocalDate lastSuccessDate;

    private int point;

    private UserActivityStats(Long userId) {
        this.userId = userId;
    }

    /**
     * 특정 사용자에 대한 새로운 활동 통계 엔티티를 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 새로 생성된 UserActivityStats 객체
     */
    public static UserActivityStats create(Long userId) {
        return new UserActivityStats(userId);
    }

    /**
     * 인증 상태 변경(성공/실패)을 기록하고 관련 통계를 업데이트합니다.
     *
     * @param isSuccess 인증 성공 여부
     */
    public void recordProofVerification(boolean isSuccess) {
        this.totalProofCount++;

        if (isSuccess) {
            this.successProofCount++;
            updateConsecutiveSuccess();
        } else {
            resetConsecutiveSuccess();
        }
    }

    private void updateConsecutiveSuccess() {
        LocalDate today = LocalDate.now();

        // 오늘 이미 성공한 기록이 있다면 연속일수를 더 올리지 않음
        if (today.equals(lastSuccessDate)) {
            return;
        }

        if (today.minusDays(1).equals(lastSuccessDate)) {
            // 어제 성공 -> 연속 성공
            this.consecutiveSuccessDays++;
        } else {
            // 연속 깨짐 또는 첫 성공
            this.consecutiveSuccessDays = 1;
        }

        this.lastSuccessDate = today;
        this.maxConsecutiveSuccessDays = Math.max(this.maxConsecutiveSuccessDays, this.consecutiveSuccessDays);
    }

    private void resetConsecutiveSuccess() {
        this.consecutiveSuccessDays = 0;
        // lastSuccessDate는 초기화하지 않음. 다음 성공 시 비교를 위해 유지.
    }
}