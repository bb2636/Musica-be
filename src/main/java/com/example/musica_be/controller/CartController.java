package com.example.musica_be.controller;

import com.example.musica_be.dto.cart.CartDto;
import com.example.musica_be.dto.cart.CartItemIdsDto;
import com.example.musica_be.dto.cart.CartResponseDto;
import com.example.musica_be.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
  private final CartService cartService;

  @GetMapping
  public CartDto getCarts(@RequestHeader("Authorization") String jwt) {
    return cartService.getCartItemList(jwt);
  }

  @PutMapping ()
  public ResponseEntity<CartResponseDto> createCart(
      @RequestHeader("Authorization") String jwt,
      @RequestParam Long classId) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(cartService.cartItemAdd(classId, jwt));
  }

  @DeleteMapping
  public ResponseEntity<CartResponseDto>  deleteCarts(
      @RequestHeader("Authorization") String jwt,
      @RequestBody(required = false) CartItemIdsDto cartItemIdsDto) {

    if (cartItemIdsDto == null || cartItemIdsDto.getCartItemIds().isEmpty()) {
      return ResponseEntity
          .ok()
          .body(cartService.cartItemAllRemove(jwt));

    } else {
      return ResponseEntity
          .ok()
          .body(cartService.cartItemRemove(jwt, cartItemIdsDto));
    }
  }
}
