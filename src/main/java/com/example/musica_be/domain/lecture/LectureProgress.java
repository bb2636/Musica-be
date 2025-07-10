package com.example.musica_be.domain.lecture;

import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "lecture_progress")
public class LectureProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 강의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // 시청 시간 (초)
    @Column(nullable = false)
    private Integer watchedSeconds;

    // 마지막 시청 시각
    private LocalDateTime lastWatchedAt;

    // 시청 완료 여부 (선택)
    private Boolean isCompleted;

    // 사용자의 강의 시청 진행 상황을 갱신하는 메서드
    public void updateProgress(int seconds) {
        // 1. 사용자가 현재까지 시청한 시간을 갱신 (초 단위)
        this.watchedSeconds = seconds;

        // 2. 마지막으로 시청한 시간을 현재 시각으로 업데이트
        this.lastWatchedAt = LocalDateTime.now();

        // 3. 전체 강의 길이와 비교하여 시청 완료 여부 결정
        // - 전체 길이 이상을 시청했다면 isCompleted = true
        if (seconds >= this.lecture.getDuration()) {
            this.isCompleted = true;
        }
    }
}
