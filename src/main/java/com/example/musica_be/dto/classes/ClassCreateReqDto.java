package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Category;
import lombok.Getter;

@Getter
public class ClassCreateReqDto {
    private String title;           // 제목
    private String descriptionHtml; // 상세 설명
    private Category category;      // 카테고리 enum
    private Long difficultyId;      // Level 엔티티의 ID
    private String thumbnailUrl;    // 썸네일 이미지 URL
    private Integer classPrice;     // 클래스 가격
}