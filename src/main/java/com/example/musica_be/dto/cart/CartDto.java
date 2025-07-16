package com.example.musica_be.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartDto {
  private Long CartId;
  private Long userId;
  private List<CartItemDto> cartItems;
  private Integer totalCount;
  private Integer totalPrice;
}
