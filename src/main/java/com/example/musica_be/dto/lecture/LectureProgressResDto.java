package com.example.musica_be.dto.lecture;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureProgressResDto {

    private Long lectureId;
    private Integer watchedSeconds;
    private Integer duration;
    private Integer progressPercent;

    public static LectureProgressResDto from(int watchedSeconds, int duration, Long lectureId) {
        int percent = (duration > 0) ? (int) ((watchedSeconds / (double) duration) * 100) : 0;

        return LectureProgressResDto.builder()
            .lectureId(lectureId)
            .watchedSeconds(watchedSeconds)
            .duration(duration)
            .progressPercent(percent)
            .build();
    }
}
