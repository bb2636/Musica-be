package com.example.musica_be.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 클래스 요약에 필요한 통계 정보 DTO
 * - 강의 수
 * - 수강생 수
 * - 평균 별점
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassStatisticsDto {
    private Long classId;
    private Long lectureCount;
    private Long studentCount;
    private Double averageRating;
}