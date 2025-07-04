package com.example.musica_be.dto.classes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopularClassResDto {
    private Long id;
    private String title;
    private String categoryName;
    private String thumbnailUrl;
    private Integer classPrice;
}