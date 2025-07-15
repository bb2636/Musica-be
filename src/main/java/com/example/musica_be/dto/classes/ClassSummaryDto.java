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
    private Long id;
    private String title;
    private String category;
    private String difficulty;
    private String thumbnailUrl;
    private Integer classPrice;
    private String instructorName;
    private int totalLectureCount;
    private int studentCount;
    private double averageRating;

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
            .build();
    }
}
