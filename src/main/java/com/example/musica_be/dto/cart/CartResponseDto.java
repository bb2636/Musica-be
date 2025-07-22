package com.example.musica_be.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto {
  private String status;
  private String message;
  private CartItemIdsDto items;

  public static CartResponseDto fail(String message) {
    return new CartResponseDto("fail", message, null);
  }
}
