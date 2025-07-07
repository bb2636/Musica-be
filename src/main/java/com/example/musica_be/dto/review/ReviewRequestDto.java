package com.example.musica_be.dto.review;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDto {
    private Integer lectureId;
    private Integer rating;
    private String comment;
}
