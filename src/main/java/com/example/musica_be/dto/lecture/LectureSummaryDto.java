package com.example.musica_be.dto.lecture;

import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureProgress;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LectureSummaryDto {

    private Long lectureId;
    private String title;
    private int order;

    // 선택: 시청률 (%)
    private Integer progressRate;

    /**
     * 공개용 또는 강사용 (진행률 없음)
     */
    public static LectureSummaryDto from(Lecture lecture) {
        return new LectureSummaryDto(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getLectureOrder(),
            null // 시청률 없음
        );
    }

    /**
     * 수강생용 (진행률 포함)
     */
    public static LectureSummaryDto from(Lecture lecture, LectureProgress progress) {
        int rate = 0;

        if (progress != null && lecture.getDuration() > 0) {
            double ratio = (double) progress.getWatchedSeconds() / lecture.getDuration();
            rate = (int) Math.min(100, Math.round(ratio * 100)); // 최대 100%
        }

        return new LectureSummaryDto(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getLectureOrder(),
            rate
        );
    }
}