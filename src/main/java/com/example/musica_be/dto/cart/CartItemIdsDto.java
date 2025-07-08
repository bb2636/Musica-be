package com.example.musica_be.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartItemIdsDto {
  List<Long> CartItemIds;
}
