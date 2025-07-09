package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Classes;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassDetailResDto {
    private Long id;
    private String title;
    private String descriptionHtml;
    private String categoryName;
    private String difficulty;
    private String thumbnailUrl;
    private Integer classPrice;
    private String instructorName;  // instructor의 이름 (또는 닉네임)

    public static ClassDetailResDto from(Classes classes) {
        return ClassDetailResDto.builder()
            .id(classes.getId())
            .title(classes.getTitle())
            .descriptionHtml(classes.getDescriptionHtml())
            .categoryName(classes.getCategory().getDisplayName())
            .difficulty(classes.getDifficulty().getName())
            .thumbnailUrl(classes.getThumbnailUrl())
            .classPrice(classes.getClassPrice())
            .instructorName(classes.getInstructor().getName())
            .build();
    }
}