package com.example.musica_be.dto.classes;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ClassUpdateReqDto {
    @NotBlank(message = "클래스 제목은 필수입니다.")
    private String title;
    private String descriptionHtml;
    @NotBlank(message = "카테고리는 필수입니다.")
    private Long categoryId;
    @NotBlank(message = "난이도는 필수입니다.")
    private Long difficultyId;
    private String thumbnailUrl;
    @NotBlank(message = "클래스 가격은 필수입니다.")
    private Integer classPrice;
    private Boolean isRecommended;
}