package com.example.musica_be.dto.lecture;

import com.example.musica_be.domain.lecture.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureWatchResDto {
    private String videoUrl;
    private Integer progress;

    public static LectureWatchResDto from(Lecture lecture) {
        return LectureWatchResDto.builder()
            .videoUrl(lecture.getVideoUrl())
            .progress(lecture.getProgress())
            .build();
    }
}