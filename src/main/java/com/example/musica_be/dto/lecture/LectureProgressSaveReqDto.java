package com.example.musica_be.dto.lecture;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LectureProgressSaveReqDto {

    @NotNull(message = "강의 ID는 필수입니다.")
    private Long lectureId;

    @NotNull(message = "진행률은 필수입니다.")
    @Min(value = 0, message = "진행률은 0 이상이어야 합니다.")
    @Max(value = 100, message = "진행률은 100 이하이어야 합니다.")
    private Integer progress;
}