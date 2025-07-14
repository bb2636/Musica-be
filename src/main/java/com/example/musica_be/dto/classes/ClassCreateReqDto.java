package com.example.musica_be.dto.classes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ClassCreateReqDto {
    @NotBlank(message = "클래스 제목은 필수입니다.")
    private String title;             // 제목

    private String descriptionHtml;   // 상세 설명

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;          // 카테고리 ID (FK)

    @NotNull(message = "난이도는 필수입니다.")
    private Long difficultyId;        // Level ID (FK)

    private String thumbnailUrl;      // 썸네일 이미지 URL

    @NotNull(message = "클래스 가격은 필수입니다.")
    private Integer classPrice;      // 클래스 가격

    private Boolean isRecommended;  // 관리자 추천 여부 (기본값 false, true 설정 시 추천 클래스로 등록)
}