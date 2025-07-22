package com.example.musica_be.controller;

import com.example.musica_be.dto.cart.CartDto;
import com.example.musica_be.dto.cart.CartItemIdsDto;
import com.example.musica_be.dto.cart.CartResponseDto;
import com.example.musica_be.service.cart.CartService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 장바구니 기능을 담당하는 컨트롤러입니다.
 * 기능: 장바구니 조회, 강의 추가, 강의 삭제(선택 또는 전체)
 */
@RestController
@RequestMapping("/api/users/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * [GET] /api/users/carts
     * 현재 로그인한 유저의 장바구니 항목 목록을 조회합니다.
     *
     * @param jwt Authorization 헤더에 담긴 JWT
     * @return CartDto (강의 목록, 총 가격 등 포함)
     */
    @GetMapping
    public CartDto getCarts(@RequestHeader("Authorization") String jwt) {
        System.out.println("컨트롤러 jwt = " + jwt);
        Long userId = JwtUtils.extractUserId(jwt);
        System.out.println("userId = " + userId);
        return cartService.getCartItemList(userId);
    }

    /**
     * [PUT] /api/users/carts?classId={id}
     * 장바구니에 새로운 강의를 추가합니다.
     *
     * @param jwt     Authorization 헤더에 담긴 JWT
     * @param classId 추가할 강의 ID (RequestParam으로 전달)
     * @return 추가 성공 여부 및 메시지를 담은 CartResponseDto
     */
    @PutMapping
    public ResponseEntity<CartResponseDto> createCart(
        @RequestHeader("Authorization") String jwt,
        @RequestParam Long classId
    ) {
        System.out.println("Authorization = " + jwt); // null이면 헤더 안 온 거임
        System.out.println("classId = " + classId);
        Long userId = JwtUtils.extractUserId(jwt);
        try {
            CartResponseDto response = cartService.cartItemAdd(userId, classId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(CartResponseDto.fail(e.getMessage()));
        }
    }

    /**
     * [DELETE] /api/users/carts
     * 장바구니에서 강의 항목 삭제 (선택 삭제 or 전체 삭제)
     *
     * @param jwt            Authorization 헤더에 담긴 JWT
     * @param cartItemIdsDto 삭제할 CartItem의 ID 리스트 (비어있으면 전체 삭제)
     * @return 삭제 결과 메시지를 담은 CartResponseDto
     */
    @DeleteMapping
    public ResponseEntity<CartResponseDto> deleteCarts(
        @RequestHeader("Authorization") String jwt,
        @RequestBody(required = false) CartItemIdsDto cartItemIdsDto
    ) {
        Long userId = JwtUtils.extractUserId(jwt);

        // 삭제 항목 없으면 전체 삭제
        if (cartItemIdsDto == null || cartItemIdsDto.getCartItemIds().isEmpty()) {
            return ResponseEntity.ok(cartService.cartItemAllRemove(userId));
        }

        // 선택 항목 삭제
        return ResponseEntity.ok(cartService.cartItemRemove(userId, cartItemIdsDto));
    }
}
