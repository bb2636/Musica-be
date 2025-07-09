package com.example.musica_be.dto.review;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDto {
    private Long  classId;
    private Long  lectureId;
    private Integer rating;
    private String comment;
}
