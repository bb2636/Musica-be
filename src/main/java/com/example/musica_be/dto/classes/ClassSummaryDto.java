package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.Level;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassSummaryDto {
    private Long id;
    private String title;
    private String category;
    private String difficulty;
    private String thumbnailUrl;
    private Integer classPrice;
    private String instructorName;
    private int totalLectureCount; // 강의 수

    public static ClassSummaryDto from(Classes c, int lectureCount) {
        return ClassSummaryDto.builder()
            .id(c.getId())
            .title(c.getTitle())
            .category(c.getCategory().getDisplayName())
            .difficulty(c.getDifficulty().getName())
            .thumbnailUrl(c.getThumbnailUrl())
            .classPrice(c.getClassPrice())
            .instructorName(c.getInstructor().getName())
            .totalLectureCount(lectureCount)
            .build();
    }
}
