package com.example.musica_be.dto.review;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private String status;
    private String message;
    private Integer reviewId;
    private String name;
    private int rating;
    private String comment;
    private int progress;
    private boolean isAuthor; // 작성자 변별목적
    private String createdAt; // 후기 최신순 정렬 목적
    private Long lectureId; // 강의 ID 추가
}