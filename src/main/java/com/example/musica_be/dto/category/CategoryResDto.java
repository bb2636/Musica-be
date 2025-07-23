package com.example.musica_be.dto.category;

import com.example.musica_be.domain.classes.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResDto {
  private Long categoryId;
  private String code;
  private String displayName;
  private boolean isActive;

  public static CategoryResDto from(Category category, String code) {
    return CategoryResDto.builder()
        .categoryId(category.getId())
        .code(code)
        .displayName(category.getDisplayName())
        .isActive(category.isActive())
        .build();
  }
}

