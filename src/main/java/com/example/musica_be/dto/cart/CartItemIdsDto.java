package com.example.musica_be.dto.cart;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor // ✅ Jackson 역직렬화를 위한 기본 생성자
public class CartItemIdsDto {
  List<Long> cartItemIds;
}
