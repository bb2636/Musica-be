package com.example.musica_be.dto.lecture;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LectureProgressSaveReqDto {

    @NotNull(message = "시청 시간은 필수입니다.")
    @Min(value = 0, message = "시청 시간은 0 이상이어야 합니다.")
    private Integer watchedSeconds;

    @NotNull(message = "완료 여부는 필수입니다.")
    @Setter  // setter 꼭 필요!
    private Boolean completed;
}