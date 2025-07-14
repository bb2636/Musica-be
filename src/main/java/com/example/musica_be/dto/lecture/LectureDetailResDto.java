package com.example.musica_be.dto.lecture;

import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureProgress;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureDetailResDto {
    private Long id;
    private String title;
    private String videoUrl; // Presigned URL
    private String fileUrl;  // Presigned URL
    private Integer lectureOrder;
    private Integer duration; // 강의 영상 길이
    private Integer progress;
    private Boolean isCompleted;

    public static LectureDetailResDto from(
        Lecture lecture,
        LectureProgress progress,
        String videoUrl,       // Presigned URL 주입
        String fileUrl         // Presigned URL 주입
    ) {
        return LectureDetailResDto.builder()
            .id(lecture.getId())
            .title(lecture.getTitle())
            .videoUrl(videoUrl)
            .fileUrl(fileUrl)
            .lectureOrder(lecture.getLectureOrder())
            .duration(lecture.getDuration())
            .progress(progress != null ? progress.getWatchedSeconds() : 0)
            .isCompleted(progress != null && Boolean.TRUE.equals(progress.getIsCompleted()))
            .build();
    }
}