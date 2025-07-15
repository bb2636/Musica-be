package com.example.musica_be.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClassesRatingAvgDto {
    private Long classId;        // 클래스 ID
    private Double averageRating; // 평균 별점
}