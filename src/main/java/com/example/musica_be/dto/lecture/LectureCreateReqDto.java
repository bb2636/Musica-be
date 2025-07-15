package com.example.musica_be.dto.lecture;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LectureCreateReqDto {
    @NotBlank(message = "강의 제목은 필수입니다.")
    private String title; // 강의 제목

    /**
     * S3에 업로드된 영상 객체 URL (옵션)
     */
    private String videoUrl;

    /**
     * S3에 업로드된 강의자료 객체 URL (옵션)
     */
    private String fileUrl;

    @NotNull(message = "강의 순서는 필수입니다.")
    private Integer lectureOrder;

    private Integer duration; // 강의 길이
}