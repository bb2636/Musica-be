package com.example.musica_be.controller;

import com.example.musica_be.dto.instructor.*;
import com.example.musica_be.service.instructor.InstructorService;
import com.example.musica_be.service.review.ReviewService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instructors")
public class InstructorController {

    private final InstructorService InstructorService;
    private final ReviewService reviewService;
    private final InstructorService instructorService;

    // 강사 마이페이지 대시보드에 보일 내용들
    @GetMapping("/dashboard")
    public ResponseEntity<InstructorDashboardResDto> getInstructorDashboard(
        @RequestHeader("Authorization") String jwt
    ) {
        Long userId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(InstructorService.getInstructorDashboard(userId));
    }

    // 특정 강사의 후기 모아보기 (최신순)
    @GetMapping("/reviews")
    public ResponseEntity<PagedResponse<InstructorReviewDto>> getInstructorReviews(
        @RequestHeader("Authorization") String jwt,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Long classId,
        @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return ResponseEntity.ok(
            instructorService.getReviewsByInstructor(jwt, page, size, classId, sort)
        );
    }

    // 특정 강사의 개인 정보 불러오기
    @GetMapping("/info")
    public ResponseEntity<InstructorInfoDto> getInstructorInfo(
        @RequestHeader("Authorization") String jwt
    ) {
        return ResponseEntity.ok(instructorService.getInstructorInfo(jwt));
    }

    // 특정 강사의 개인 정보 수정
    @PutMapping("/info")
    public ResponseEntity<InstructorInfoDto> updateInstructorInfo(
        @RequestHeader("Authorization") String jwt,
        @RequestBody InstructorUpdateRequestDto dto
    ) {
        InstructorInfoDto updatedInfo = instructorService.updateInstructorInfo(jwt, dto);
        return ResponseEntity.ok(updatedInfo);
    }

}
