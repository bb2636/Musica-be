package com.example.musica_be.controller;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.review.ReviewRequestDto;
import com.example.musica_be.dto.review.ReviewResponseDto;
import com.example.musica_be.dto.review.UpdateReviewDto;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.service.review.ReviewService;
import com.example.musica_be.util.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    private User getUser(String jwt) {
        Long userId = JwtUtils.extractUserId(jwt);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
    }

    // 리뷰 등록
    @PostMapping
    @Transactional
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestHeader("Authorization") String jwt,
            @RequestBody ReviewRequestDto requestDto) {

        User user = getUser(jwt);
        ReviewResponseDto response = reviewService.createReview(user, requestDto);
        return ResponseEntity.ok(response);
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Integer reviewId,
            @RequestBody UpdateReviewDto dto) {
        User user = getUser(jwt);
        ReviewResponseDto response = reviewService.updateReview(user, reviewId, dto);
        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> deleteReview(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Integer reviewId) {
        User user = getUser(jwt);
        ReviewResponseDto response = reviewService.deleteReview(user, reviewId);
        return ResponseEntity.ok(response);
    }




    // 1. 유저 개인용 후기 목록 (마이페이지)
    @GetMapping("/mypage")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviewss(
            @RequestHeader("Authorization") String jwt) {
        User user = getUser(jwt);
        return ResponseEntity.ok(reviewService.getReviewsByUser(user));
    }

    // 2. 클래스별 후기 전체 조회 (공개)
    @GetMapping("/classes/{classId}")
    public ResponseEntity<List<ReviewResponseDto>> getClassReviews(
            @PathVariable Long classId,
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        Long userId = null;
        // Authorization 헤더가 있을 때만 시도 (없으면 로그인 안 한 상태로 간주)
        if(jwt != null && jwt.startsWith("Bearer ")) {
            try {
                userId = JwtUtils.extractUserId(jwt);
            } catch (Exception e) {
                // 실패해도 무시하고 null 유지 (공개 API 여서)
                userId = null;
            }
        }

        return ResponseEntity.ok(reviewService.getReviewsByClass(classId, userId));
    }

    // 3. 후기 단건 조회 (공개)
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewById(
            @PathVariable Integer reviewId,
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        Long userId = null;
        if(jwt != null && jwt.startsWith("Bearer ")) {
            try {
                userId = JwtUtils.extractUserId(jwt);
            } catch (Exception e) {
                userId = null;
            }
        }
        return ResponseEntity.ok(reviewService.getReviewById(reviewId, userId));
    }

    // 4. 특정 강의의 후기 요약 (AI 활용)
    @GetMapping("/summary/lecture/{lectureId}")
    public ResponseEntity<Map<String, String>> summarizeReviews(@PathVariable Long lectureId) {
        // 1. 강의에 대한 raw 댓글 모음
        String rawComments = reviewService.getRawCommentsByLecture(lectureId);

        // 2. OpenAI를 통해 요약
        String summary = reviewService.summarizeWithOpenAI(rawComments);

        // 3. JSON 형태로 반환
        return ResponseEntity.ok(Map.of(
                "lectureId", lectureId.toString(),
                "summary", summary
        ));
    }

    // 5. 인증 실패 대응 ( JWT 파싱 실패시 )
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
