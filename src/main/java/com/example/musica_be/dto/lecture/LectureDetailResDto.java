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
    private String videoUrl;
    private String fileUrl;
    private Integer lectureOrder;
    private Integer duration;         // 강의 전체 길이
    private Integer progress;         // 사용자의 시청 시간
    private Boolean isCompleted;      // 시청 완료 여부

    public static LectureDetailResDto from(Lecture lecture, LectureProgress progress) {
        return LectureDetailResDto.builder()
            .id(lecture.getId())
            .title(lecture.getTitle())
            .videoUrl(lecture.getVideoUrl())
            .fileUrl(lecture.getFileUrl())
            .lectureOrder(lecture.getLectureOrder())
            .duration(lecture.getDuration())
            .progress(progress != null ? progress.getWatchedSeconds() : 0)
            .isCompleted(progress != null && Boolean.TRUE.equals(progress.getIsCompleted()))
            .build();
    }
}