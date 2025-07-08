package com.example.musica_be.controller;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.review.ReviewRequestDto;
import com.example.musica_be.dto.review.ReviewResponseDto;
import com.example.musica_be.dto.review.UpdateReviewDto;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.service.ReviewService;
import com.example.musica_be.util.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    private User getUser(String authHeader) {
        Long userId = Long.valueOf(JwtUtils.getUserIdFromToken(authHeader));

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
    }

    // 리뷰 등록
    @PostMapping
    @Transactional
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ReviewRequestDto requestDto) {

        User user = getUser(authHeader);
        ReviewResponseDto response = reviewService.createReview(user, requestDto);
        return ResponseEntity.ok(response);
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer reviewId,
            @RequestBody UpdateReviewDto dto) {
        User user = getUser(authHeader);
        ReviewResponseDto response = reviewService.updateReview(user, reviewId, dto);
        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer reviewId) {
        User user = getUser(authHeader);
        ReviewResponseDto response = reviewService.deleteReview(user, reviewId);
        return ResponseEntity.ok(response);
    }

    // 후기 목록 조회 (강의 기준)
    @GetMapping("/lecture/{lectureId}")
    public ResponseEntity<List<ReviewResponseDto>> getLectureReviews(
            @PathVariable Integer lectureId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = authHeader != null ? Long.valueOf(JwtUtils.getUserIdFromToken(authHeader)) : null;
        return ResponseEntity.ok(reviewService.getReviewsByLecture(lectureId, userId));
    }

    // 후기 단건 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewById(
            @PathVariable Integer reviewId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = authHeader != null ? Long.valueOf(JwtUtils.getUserIdFromToken(authHeader)) : null;
        return ResponseEntity.ok(reviewService.getReviewById(reviewId, userId));
    }

    // 내 후기 목록 조회
    @GetMapping("/mypage")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews(
            @RequestHeader("Authorization") String authHeader) {
        User user = getUser(authHeader);
        return ResponseEntity.ok(reviewService.getReviewsByUser(user));
    }

}
