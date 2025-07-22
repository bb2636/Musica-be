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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(nullable = false)
    private Integer watchedSeconds;

    private LocalDateTime lastWatchedAt;

    private Boolean isCompleted;

    public void updateProgress(int seconds) {
        this.watchedSeconds = seconds;
        this.lastWatchedAt = LocalDateTime.now();

        int duration = this.lecture.getDuration();
        if (duration > 0 && seconds >= duration * 0.95) {
            this.isCompleted = true;
        } else {
            this.isCompleted = false;
        }
    }

    public static LectureProgress create(User user, Lecture lecture) {
        return LectureProgress.builder()
            .user(user)
            .lecture(lecture)
            .watchedSeconds(0)
            .isCompleted(false)
            .lastWatchedAt(LocalDateTime.now())
            .build();
    }

    // ✅ missing setter 추가
    public void setCompleted(Boolean completed) {
        this.isCompleted = completed;
    }

    public Boolean getIsCompleted() {
        return this.isCompleted != null && this.isCompleted;
    }
}