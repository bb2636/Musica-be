package com.example.musica_be.domain.lecture;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.example.musica_be.domain.classes.Classes;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Classes classes;

    @Column(nullable = false)
    private String title;

    // 사용자가 실제 재생할 URL
    @Column(nullable = false)
    private String videoUrl;

    private Integer progress;

    private String sheetMusicUrl;

    // S3 내부에서 Presigned URL 만들 때 사용하는 경로
    @Column(nullable = false)
    private String videoObjectKey;  // 예: "lectures/123/강의1.mp4"

    @Column(nullable = false)
    private Integer lectureOrder;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Lecture.java
    public void update(String title, String videoUrl, String sheetMusicUrl, int lectureOrder) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.sheetMusicUrl = sheetMusicUrl;
        this.lectureOrder = lectureOrder;
    }

    public void changeOrder(int newOrder) {
        this.lectureOrder = newOrder;
    }

    public void updateProgress(Integer progress) {
        this.progress = progress;
    }
}