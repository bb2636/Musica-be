package com.example.musica_be.domain;

import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 👇 임시로 강의 ID만 저장
    @Column(name = "lecture_id", nullable = false)
    private Integer lectureId;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // 수정 가능 필드만 setter 열기
    public void update(String comment, Integer rating) {
        this.comment = comment;
        this.rating = rating;
    }

}
