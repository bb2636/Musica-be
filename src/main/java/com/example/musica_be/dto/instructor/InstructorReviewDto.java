package com.example.musica_be.dto.instructor;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorReviewDto {
    private Integer reviewId;
    private String reviewerName;  // 수강생 이름
    private String comment;
    private int rating;
    private String createdAt;

    private Long classId;
    private String classTitle;

    private Long lectureId;
    private String lectureTitle;
}