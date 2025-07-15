package com.example.musica_be.controller;

import com.example.musica_be.dto.classes.ClassCardDto;
import com.example.musica_be.dto.review.ReviewSummaryCardDto;
import com.example.musica_be.service.classes.ClassesService;
import com.example.musica_be.service.review.ReviewService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainPageController {
    private final ClassesService classesService;
    private final ReviewService reviewService;

    // 개인화 추천 클래스 API
    @GetMapping("/recommend")
    public ResponseEntity<List<ClassCardDto>> getRecommendedClasses(
            @RequestHeader("Authorization") String jwt) {

        Long userId = JwtUtils.extractUserId(jwt);
        String role = JwtUtils.extractRole(jwt);

        if (!"USER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ClassCardDto> result = classesService.getRecommendedClasses(userId);
        return ResponseEntity.ok(result);
    }

    // 인기 클래스 조회
    @GetMapping("/popular")
    public ResponseEntity<List<ClassCardDto>> getPopularClasses() {
        return ResponseEntity.ok(classesService.getPopularClasses());
    }

    // 최신 클래스 조회
    @GetMapping("/latest")
    public ResponseEntity<List<ClassCardDto>> getLatestClasses() {
        return ResponseEntity.ok(classesService.getLatestClasses());
    }

    // 후기 요약 카드 조회
    @GetMapping("/reviews/summary")
    public ResponseEntity<List<ReviewSummaryCardDto>> getReviewsSummaryCard() {
        return ResponseEntity.ok(reviewService.getReviewSummaryCards());
    }

    // 무료클래스 4개 조회
    @GetMapping("/classes/free")
    public ResponseEntity<List<ClassCardDto>> getFreeClassCards() {
        return ResponseEntity.ok(classesService.getFreeClassCards());
    }
}
