package com.example.musica_be.dto.cart;

import com.example.musica_be.domain.cart.CartItem;
import com.example.musica_be.domain.classes.Classes;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDto {
  private Long classId;
  private String title;
  private String thumbnailUrl;
  private String instructorName;
  private Integer price;

  public static CartItemDto fromClasses (Classes classes) {
    return CartItemDto.builder()
        .classId(classes.getId())
        .title(classes.getTitle())
        .thumbnailUrl(classes.getThumbnailUrl())
        .price(classes.getClassPrice())
        .build();
  }
}


