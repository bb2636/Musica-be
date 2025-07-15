package com.example.musica_be.dto.classes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ClassUpdateReqDto {
    @NotBlank(message = "클래스 제목은 필수입니다.")
    private String title;
    private String descriptionHtml;
    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;
    @NotNull(message = "난이도는 필수입니다.")
    private Long difficultyId;
    private String thumbnailUrl;
    @NotNull(message = "클래스 가격은 필수입니다.")
    private Integer classPrice;
}