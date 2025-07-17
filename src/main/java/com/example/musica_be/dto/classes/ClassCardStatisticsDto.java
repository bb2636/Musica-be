package com.example.musica_be.dto.classes;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClassCardStatisticsDto {
    private Long classId;
    private Long studentCount;
    private Long wishlistCount;
    private Double averageRating;
    private Long ratingCount;

    // JPQL DTO 매핑 오류 방지 생성자
    // JPA 쿼리용 유연한 생성자 (Number 타입용)
    public ClassCardStatisticsDto(Number classId, Number studentCount, Number wishlistCount, Number averageRating, Number ratingCount) {
        this.classId = classId != null ? classId.longValue() : 0L;
        this.studentCount = studentCount != null ? studentCount.longValue() : 0L;
        this.wishlistCount = wishlistCount != null ? wishlistCount.longValue() : 0L;
        this.averageRating = averageRating != null ? averageRating.doubleValue() : 0.0;
        this.ratingCount = ratingCount != null ? ratingCount.longValue() : 0L;
    }

    // 정확 타입 생성자 (JPA가 이걸 우선 찾게끔)
    public ClassCardStatisticsDto(Long classId, Long studentCount, Long wishlistCount, Double averageRating, Long ratingCount) {
        this.classId = classId;
        this.studentCount = studentCount;
        this.wishlistCount = wishlistCount;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    // 통계 없는 클래스 구분 시 매우 유용
    public boolean isEmpty() {
        return (studentCount == null || studentCount == 0L)
                && (wishlistCount == null || wishlistCount == 0L)
                && (averageRating == null || averageRating == 0.0)
                && (ratingCount == null || ratingCount == 0L);
    }
}