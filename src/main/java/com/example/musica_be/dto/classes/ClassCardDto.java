package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Classes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassCardDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private int price;
    private double rating;
    private String categoryName;

    public static ClassCardDto from(Classes cls, double rating) {
        return ClassCardDto.builder()
                .id(cls.getId())
                .title(cls.getTitle())
                .thumbnailUrl(cls.getThumbnailUrl())
                .price(cls.getClassPrice())
                .rating(rating)
                .categoryName(cls.getCategory().getDisplayName())
                .build();
    }
}
