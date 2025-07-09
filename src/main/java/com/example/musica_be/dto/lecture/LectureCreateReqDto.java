package com.example.musica_be.dto.lecture;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LectureCreateReqDto {

    @NotNull(message = "클래스 ID는 필수입니다.")
    private Long classId;

    @NotBlank(message = "강의 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "영상 URL은 필수입니다.")
    private String videoUrl;

    private String sheetMusicUrl; // 선택값

    @NotNull(message = "강의 순서는 필수입니다.")
    private Integer lectureOrder;


}