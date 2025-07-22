package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Classes;
import lombok.Builder;
import lombok.Getter;

/**
 * 클래스 목록에 표시할 요약 정보(DTO)
 * - 강의 리스트 화면 등에서 각 클래스를 요약 형태로 보여줄 때 사용
 * - 필요한 정보만 추려서 전달하는 용도의 DTO
 */
@Builder
@Getter
public class ClassSummaryDto {

    /** 클래스 ID */
    private Long id;

    /** 클래스 제목 */
    private String title;

    /** 클래스 카테고리 (예: 피아노, 기타 등) */
    private String category;

    /** 클래스 난이도 (예: 초급, 중급, 고급) */
    private String difficulty;

    /** 썸네일 이미지 URL */
    private String thumbnailUrl;

    /** 클래스 가격 */
    private Integer classPrice;

    /** 강사 이름 */
    private String instructorName;

    /** 해당 클래스에 포함된 전체 강의 수 */
    private int totalLectureCount;

    /** 이 클래스를 수강 중인 총 수강생 수 */
    private int studentCount;

    /** 수강생들의 평균 별점 (0.0 ~ 5.0) */
    private double averageRating;

    // 별점 수 ( == 리뷰 수 )
    private int ratingCount;

    /**
     * 클래스 엔티티와 통계 정보를 기반으로 ClassSummaryDto 생성
     *
     * @param c 클래스 엔티티
     * @param stat 클래스 통계 정보 (null일 수 있음)
     * @return 생성된 ClassSummaryDto
     */
    public static ClassSummaryDto from(Classes c, ClassStatisticsDto stat) {
        return ClassSummaryDto.builder()
            .id(c.getId())
            .title(c.getTitle())
            .category(c.getCategory().getDisplayName())
            .difficulty(c.getDifficulty().getName())
            .thumbnailUrl(c.getThumbnailUrl())
            .classPrice(c.getClassPrice())
            .instructorName(c.getInstructor().getName())
            .totalLectureCount(stat != null ? stat.getLectureCount().intValue() : 0)
            .studentCount(stat != null ? stat.getStudentCount().intValue() : 0)
            .averageRating(stat != null ? stat.getAverageRating() : 0.0)
            .ratingCount(stat != null ? stat.getRatingCount().intValue() : 0)
            .build();
    }
}