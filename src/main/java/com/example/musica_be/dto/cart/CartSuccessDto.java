package com.example.musica_be.dto.cart;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartSuccessDto {
  String status;
  String message;
}
