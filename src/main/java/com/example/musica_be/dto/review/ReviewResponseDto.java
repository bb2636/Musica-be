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
}