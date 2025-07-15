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
        this.watchedSeconds = seconds;
        this.lastWatchedAt = LocalDateTime.now();

        // 전체 강의 길이의 95% 이상을 시청했으면 완료로 처리
        int duration = this.lecture.getDuration();  // 강의 길이 (초)

        if (duration > 0 && seconds >= duration * 0.95) {
            this.isCompleted = true;
        } else {
            this.isCompleted = false; // 기준 미달이면 false 처리 (갱신)
        }
    }
}
