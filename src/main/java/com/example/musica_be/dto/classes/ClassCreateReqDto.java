package com.example.musica_be.dto.classes;

import lombok.Getter;

@Getter
public class ClassCreateReqDto {
    private String title;           // 제목
    private String descriptionHtml; // 상세 설명
    private Long categoryId;        // 카테고리 엔티티의 ID (FK)
    private Long difficultyId;      // Level 엔티티의 ID (FK)
    private String thumbnailUrl;    // 썸네일 이미지 URL
    private Integer classPrice;     // 클래스 가격
    private Boolean isRecommended;  // 관리자 추천 여부 (기본값 false, true 설정 시 추천 클래스로 등록)
}