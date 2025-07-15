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

    // s3 에 저장된 강의 영상의 객체 url
    // aws 권한이 없으면 접근 불가 (403)
    @Column(nullable = true)
    private String videoUrl;

    // s3 에 저장된 영상 외 강의 자료의 객체 url
    // aws 권한이 없으면 접근 불가 (403)
    @Column(nullable = true)
    private String fileUrl;

    // Presigned GET URL 생성에 사용됨
    // 여기서 https://버킷명.s3.amazonaws.com/ 다음의 경로가 videoObjectKey
    @Column(nullable = true)
    private String videoObjectKey; // 예: "lectures/123/강의1.mp4"

    // Presigned GET URL 생성에 사용됨
    @Column(nullable = true)
    private String fileObjectKey; // 예: "lectures/123/강의자료1.pdf"

    // 강의 순서
    @Column(nullable = false)
    private Integer lectureOrder;

    // 강의 길이 (초 단위 저장, 프론트에서 분:초 변환)
    @Column(nullable = true)
    private Integer duration;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Lecture.java
    public void update(String title, String videoUrl, String fileUrl,
                       int lectureOrder, String videoObjectKey, String fileObjectKey,
                       Integer duration) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.fileUrl = fileUrl;
        this.lectureOrder = lectureOrder;
        this.videoObjectKey = videoObjectKey;
        this.fileObjectKey = fileObjectKey;
        this.duration = duration;
    }

    public void changeOrder(int newOrder) {
        this.lectureOrder = newOrder;
    }
}