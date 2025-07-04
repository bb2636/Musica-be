package com.example.musica_be.dto.classes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyClassListResDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private Integer classPrice;
    private String createdAt;
}
