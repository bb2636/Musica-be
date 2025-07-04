package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Category;
import lombok.Getter;

@Getter
public class CreateClassReqDto {
    private String title;
    private String descriptionHtml;
    private Category category;
    private Long difficultyId;   // Level 엔티티의 ID
    private String thumbnailUrl;
    private Integer classPrice;
}