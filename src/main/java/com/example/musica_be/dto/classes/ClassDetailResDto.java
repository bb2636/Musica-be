package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.dto.lecture.LectureSummaryDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClassDetailResDto {
    private Long id;
    private String title;
    private String descriptionHtml;
    private String categoryName;
    private String difficulty;
    private String thumbnailUrl;
    private Integer classPrice;
    private String instructorName;
    private UserClassStatus userClassStatus; // 사용자 관련 정보 - 수강 여부, 수강률
    private List<LectureSummaryDto> lectures; // 각 강의별 진행률 포함

    public static ClassDetailResDto from(Classes classes, UserClassStatus userStatus, List<LectureSummaryDto> lectures) {
        return ClassDetailResDto.builder()
            .id(classes.getId())
            .title(classes.getTitle())
            .descriptionHtml(classes.getDescriptionHtml())
            .categoryName(classes.getCategory().getDisplayName())
            .difficulty(classes.getDifficulty().getName())
            .thumbnailUrl(classes.getThumbnailUrl())
            .classPrice(classes.getClassPrice())
            .instructorName(classes.getInstructor().getName())
            .userClassStatus(userStatus)
            .lectures(lectures)
            .build();
    }

    @Getter
    @Builder
    public static class UserClassStatus {
        private boolean isEnrolled;            // 수강 중 여부
        private double progressRate;           // 수강률 (%)
        private int completedLectureCount;     // 완료한 강의 수
        private int totalLectureCount;         // 전체 강의 수
    }
}