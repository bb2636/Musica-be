package com.example.musica_be.controller.user;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.dto.category.CategoryReqDto;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.service.category.CategoryService;
import com.example.musica_be.service.user.AdminService;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CategoryService categoryService;

    //관리자 마이페이지
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/mypage")
    public ResponseEntity<UserResDto> getAdminMypage(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserResDto(user));
    }
    //전체 강사 상태 목록 확인
    @GetMapping("/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllInstructors() {
        List<User> instructors = adminService.getAllInstructors();
        return ResponseEntity.ok(instructors);
    }
    // 승인 대기 중인 강사 목록 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/instructors/pending")
    public ResponseEntity<List<User>> getPendingInstructors() {
        List<User> pendingInstructors = adminService.getPendingInstructors();
        return ResponseEntity.ok(pendingInstructors);
    }

    // 강사 승인
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/instructors/{userId}/approve")
    public ResponseEntity<String> approveInstructor(@PathVariable Long userId) {
        try {
            adminService.approveInstructor(userId);
            return ResponseEntity.ok("Instructor approved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 강사 거절
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/instructors/{userId}/reject")
    public ResponseEntity<String> rejectInstructor(@PathVariable Long userId) {
        try {
            adminService.rejectInstructor(userId);
            return ResponseEntity.ok("Instructor rejected successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    //관리자 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody LoginReqDto loginReqDto) {
        try {
            // 로그인 성공 시 JWT 토큰 반환
            Map<String, String> tokens = adminService.adminLogin(loginReqDto);
            return ResponseEntity.ok(tokens); // JSON 형태로 반환
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage())); // 실패 시 에러 메시지 반환
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody CategoryReqDto dto) {
        try {
            Category saved = categoryService.createCategory(dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 에러 발생: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryReqDto dto) {
        try {
            Category updated = categoryService.updateCategory(id, dto);
            return ResponseEntity.ok(updated); // 수정된 카테고리 반환
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); // 유효성 실패 등
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "서버 에러 발생: " + e.getMessage())); // 예외 대응
        }
    }
}
