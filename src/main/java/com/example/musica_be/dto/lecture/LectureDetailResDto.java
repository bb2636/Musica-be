package com.example.musica_be.dto.lecture;

import com.example.musica_be.domain.lecture.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureDetailResDto {
    private Long id;
    private String title;
    private String videoUrl;
    private String sheetMusicUrl;
    private Integer lectureOrder;

    public static LectureDetailResDto from(Lecture lecture) {
        return LectureDetailResDto.builder()
            .id(lecture.getId())
            .title(lecture.getTitle())
            .videoUrl(lecture.getVideoUrl())
            .sheetMusicUrl(lecture.getSheetMusicUrl())
            .lectureOrder(lecture.getLectureOrder())
            .build();
    }
}