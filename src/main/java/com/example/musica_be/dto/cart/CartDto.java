package com.example.musica_be.dto.cart;

import java.util.List;

public class CartDto {
  private Long userId;
  private List<CartItemDto> cartItems;
  private Integer totalCount;
  private Integer totalPrice;
  private Integer totalDiscountPrice;
}
