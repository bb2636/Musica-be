package com.example.musica_be.dto.classes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassDetailResDto {
    private Long id;
    private String title;
    private String descriptionHtml;
    private String categoryName;     // "피아노"
    private String difficultyName;   // "Beginner"
    private String thumbnailUrl;
    private Integer classPrice;
    private String instructorName;
    private String instructorEmail;
    private String createdAt;
}