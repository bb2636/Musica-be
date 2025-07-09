package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Category;
import lombok.Getter;

@Getter
public class ClassSearchReqDto {
    private String keyword;          // 제목/설명 키워드
    private Category category;       // 선택한 카테고리
    private Long levelId;            // 선택한 난이도
    private Integer minPrice;
    private Integer maxPrice;
}