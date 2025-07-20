package com.example.musica_be.controller;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.wishlist.WishlistActionResponseDto;
import com.example.musica_be.dto.wishlist.WishlistClassListResponseDto;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.service.wishlist.WishlistService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/wishlists")
public class WishlistController {

    private final WishlistService wishlistService;

    // 찜 추가
    @PostMapping("/classes/{classId}")
    public ResponseEntity<WishlistActionResponseDto> addWishlist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long classId) {
        Long userId = JwtUtils.extractUserId(jwt);
        WishlistActionResponseDto response = wishlistService.addWishlist(userId, classId);
        return ResponseEntity.ok(response);
    }

    // 찜 삭제
    @DeleteMapping("/classes/{classId}")
    public ResponseEntity<WishlistActionResponseDto> deleteWishlist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long classId) {
        Long userId = JwtUtils.extractUserId(jwt);
        WishlistActionResponseDto response = wishlistService.deleteWishlist(userId, classId);
        return ResponseEntity.ok(response);
    }

    // 찜 목록 조회 (마이페이지)
    @GetMapping("/mywishlist")
    public ResponseEntity<WishlistClassListResponseDto> getWishList(
            @RequestHeader("Authorization") String jwt) {
        Long userId = JwtUtils.extractUserId(jwt);
        WishlistClassListResponseDto response = wishlistService.getWishlistClasses(userId);
        return ResponseEntity.ok(response);
    }

}