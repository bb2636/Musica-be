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

    // 선택: 전체 강의 길이 (초 단위)
    private Integer duration;

    // 선택: 시청 완료 여부
    private Boolean isCompleted;

    /**
     * 공개용 또는 강사용 (진행률 없음)
     */
    public static LectureSummaryDto from(Lecture lecture) {
        return new LectureSummaryDto(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getLectureOrder(),
            null,        // progressRate
            lecture.getDuration(),     // duration 포함
            null                       // isCompleted 없음
        );
    }

    /**
     * 수강생용 (진행률 포함)
     */
    public static LectureSummaryDto from(Lecture lecture, LectureProgress progress) {
        int rate = 0;
        boolean completed = false;

        if (progress != null && lecture.getDuration() != null && lecture.getDuration() > 0) {
            double ratio = (double) progress.getWatchedSeconds() / lecture.getDuration();
            rate = (int) Math.min(100, Math.round(ratio * 100));
            completed = Boolean.TRUE.equals(progress.getIsCompleted());
        }

        return new LectureSummaryDto(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getLectureOrder(),
            rate,
            lecture.getDuration(),
            completed
        );
    }
}