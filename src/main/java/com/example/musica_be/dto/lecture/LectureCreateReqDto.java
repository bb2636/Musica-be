package com.example.musica_be.dto.lecture;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LectureCreateReqDto {

    @NotBlank(message = "강의 제목은 필수입니다.")
    private String title;

    /**
     * 강의 영상 정적 URL (프론트에 표시용)
     */
    private String videoUrl;

    /**
     * 강의 자료 정적 URL (프론트에 표시용)
     */
    private String fileUrl;

    /**
     * 강의 영상의 S3 ObjectKey (분석용)
     */
    private String videoObjectKey;

    /**
     * 강의 자료의 S3 ObjectKey (분석 X)
     */
    private String fileObjectKey;

    @NotNull(message = "강의 순서는 필수입니다.")
    private Integer lectureOrder;

    private Integer duration; // 초 단위
}