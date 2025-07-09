package com.example.musica_be.dto.classes;

import lombok.Getter;

@Getter
public class ClassUpdateReqDto {
    private String title;
    private String descriptionHtml;
    private Long categoryId;
    private Long difficultyId;
    private String thumbnailUrl;
    private Integer classPrice;
}