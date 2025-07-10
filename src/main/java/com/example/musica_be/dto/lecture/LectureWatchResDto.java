package com.example.musica_be.dto.lecture;

import com.example.musica_be.domain.lecture.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureWatchResDto {
    private Long lectureId;
    private String title;
    private String videoUrl;       // presigned GET URL (nullable)
    private String fileUrl;        // presigned GET URL (nullable)
    private Integer progress;

    public static LectureWatchResDto from(Lecture lecture, String videoUrl, String fileUrl) {
        return LectureWatchResDto.builder()
            .lectureId(lecture.getId())
            .title(lecture.getTitle())
            .videoUrl(videoUrl)
            .fileUrl(fileUrl)
            .build();
    }
}