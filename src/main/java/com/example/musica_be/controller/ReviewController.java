package com.example.musica_be.controller;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.review.ReviewRequestDto;
import com.example.musica_be.dto.review.ReviewResponseDto;
import com.example.musica_be.dto.review.UpdateReviewDto;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.service.ReviewService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    private User getUserFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = JwtUtils.getEmailFromToken(token);

        // Optional<User> → 값 없으면 예외 던지고, 있으면 User로 꺼내줌
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
    }

    // 리뷰 작성
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ReviewRequestDto requestDto) {

        User user = getUserFromToken(authHeader);
        ReviewResponseDto response = reviewService.createReview(user, requestDto);
        return ResponseEntity.ok(response);
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer reviewId,
            @RequestBody UpdateReviewDto dto) {
        User user = getUserFromToken(authHeader);
        ReviewResponseDto response = reviewService.updateReview(user, reviewId, dto);
        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer reviewId) {
        User user = getUserFromToken(authHeader);
        ReviewResponseDto response = reviewService.deleteReview(user, reviewId);
        return ResponseEntity.ok(response);
    }

}
