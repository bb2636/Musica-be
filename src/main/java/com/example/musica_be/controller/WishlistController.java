package com.example.musica_be.controller;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.wishlist.WishlistActionResponseDto;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.service.wishlist.WishlistService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlists")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @PostMapping("/classes/{classId}")
    public ResponseEntity<WishlistActionResponseDto> addWishlist(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long classId) {
        // 1. 토큰에서 "Bearer " 제거
        String token = authHeader.replace("Bearer ", "");

        // 2. 이메일 추출
        String email = JwtUtils.getEmailFromToken(token);
        System.out.println("파싱된 이메일: " + email);

        // 3. 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        System.out.println("유저 ID: " + user.getId());

        // 4. userId를 서비스에 전달
        WishlistActionResponseDto response = wishlistService.addWishlist(user.getId(), classId);
        System.out.println("찜 응답: " + response.getMessage());

        return ResponseEntity.ok(response);
    }


}
