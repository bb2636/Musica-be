package com.example.musica_be.dto.lecture;

import com.example.musica_be.domain.lecture.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureSummaryDto {
    private Long id;
    private String title;
    private Integer lectureOrder;

    public static LectureSummaryDto from(Lecture lecture) {
        return LectureSummaryDto.builder()
            .id(lecture.getId())
            .title(lecture.getTitle())
            .lectureOrder(lecture.getLectureOrder())
            .build();
    }
}