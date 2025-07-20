package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.classes.Classes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassCardDto {
    // 메인 페이지 클래스 카드 ( 추천, 인기 등 )
    private Long id;
    private String title;
    private String thumbnailUrl;
    private int price;
    private double rating;
    private String categoryName;

    // 통계용
    private int ratingCount;
    private int studentCount;
    private int wishlistCount;

    public static ClassCardDto from(Classes cls, ClassCardStatisticsDto stats) {
        if (stats == null || stats.isEmpty()) {
            return ClassCardDto.builder()
                    .id(cls.getId())
                    .title(cls.getTitle())
                    .thumbnailUrl(cls.getThumbnailUrl())
                    .price(cls.getClassPrice())
                    .rating(0.0)
                    .ratingCount(0)
                    .studentCount(0)
                    .wishlistCount(0)
                    .categoryName(cls.getCategory().getDisplayName())
                    .build();
        }

        return ClassCardDto.builder()
                .id(cls.getId())
                .title(cls.getTitle())
                .thumbnailUrl(cls.getThumbnailUrl())
                .price(cls.getClassPrice())
                .rating(Optional.ofNullable(stats.getAverageRating()).orElse(0.0))
                .ratingCount(Optional.ofNullable(stats.getRatingCount()).orElse(0L).intValue())
                .studentCount(Optional.ofNullable(stats.getStudentCount()).orElse(0L).intValue())
                .wishlistCount(Optional.ofNullable(stats.getWishlistCount()).orElse(0L).intValue())
                .categoryName(Optional.ofNullable(cls.getCategory())
                        .map(Category::getDisplayName)
                        .orElse("미지정"))
                .build();
    }
}
