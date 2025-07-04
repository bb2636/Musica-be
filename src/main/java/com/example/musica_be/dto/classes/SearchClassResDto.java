package com.example.musica_be.dto.classes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchClassResDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String categoryName;  // 예: "피아노"
    private String levelName;     // 예: "Beginner"
    private Integer classPrice;
    private String instructorName;
}