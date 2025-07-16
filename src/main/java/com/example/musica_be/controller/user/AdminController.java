package com.example.musica_be.controller.user;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.dto.category.CategoryReqDto;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.dto.user.InstructorDto; // 새로 추가
import com.example.musica_be.service.category.CategoryService;
import com.example.musica_be.service.user.AdminService;
import com.example.musica_be.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")  // ✅ 이 부분이 /api/admin인지 확인!
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CategoryService categoryService;

    // 🔧 관리자 마이페이지 (URL 경로 수정)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/mypage")
    public ResponseEntity<UserResDto> getAdminMypage(@AuthenticationPrincipal User user) {
        log.info("관리자 마이페이지 접근: {}", user.getEmail());
        return ResponseEntity.ok(new UserResDto(user));
    }

    // 🔧 전체 강사 목록 - DTO로 변환하여 반환
    @GetMapping("/instructors/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InstructorDto>> getAllInstructors() {
        try {
            log.info("🔍 전체 강사 목록 조회 요청 받음"); // 이 로그가 찍히는지 확인
            List<InstructorDto> instructors = adminService.getAllInstructorsDto();
            log.info("🔍 전체 강사 목록 조회 성공: {}명", instructors.size());
            return ResponseEntity.ok(instructors);
        } catch (Exception e) {
            log.error("전체 강사 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔧 승인 대기 중인 강사 목록 - DTO로 변환하여 반환
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/instructors/pending")
    public ResponseEntity<List<InstructorDto>> getPendingInstructors() {
        try {
            log.info("🔍 승인 대기 강사 목록 조회 요청 받음"); // 이 로그가 찍히는지 확인
            List<InstructorDto> pendingInstructors = adminService.getPendingInstructorsDto();
            log.info("🔍 승인 대기 강사 목록 조회 성공: {}명", pendingInstructors.size());
            return ResponseEntity.ok(pendingInstructors);
        } catch (Exception e) {
            log.error("승인 대기 강사 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔍 디버깅용 - 단순 강사 목록 조회 (임시)
    @GetMapping("/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getInstructorsDebug() {
        log.info("🔍 /api/admin/instructors 경로로 요청 받음 - 디버깅용");
        return ResponseEntity.ok("강사 관련 API 접근 성공! /all 또는 /pending을 사용하세요.");
    }

    // 🔧 강사 승인 - 더 상세한 에러 처리
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/instructors/{userId}/approve")
    public ResponseEntity<Map<String, Object>> approveInstructor(@PathVariable Long userId) {
        try {
            log.info("강사 승인 요청: userId={}", userId);
            adminService.approveInstructor(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "강사가 성공적으로 승인되었습니다.",
                    "userId", userId
            ));
        } catch (IllegalArgumentException e) {
            log.warn("강사 승인 실패 - 잘못된 요청: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "userId", userId
            ));
        } catch (IllegalStateException e) {
            log.warn("강사 승인 실패 - 상태 오류: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(409).body(Map.of( // 409 Conflict
                    "success", false,
                    "message", e.getMessage(),
                    "userId", userId
            ));
        } catch (Exception e) {
            log.error("강사 승인 중 서버 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 내부 오류가 발생했습니다.",
                    "userId", userId
            ));
        }
    }

    // 🔧 강사 거절 - 더 상세한 에러 처리
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/instructors/{userId}/reject")
    public ResponseEntity<Map<String, Object>> rejectInstructor(@PathVariable Long userId) {
        try {
            log.info("강사 거절 요청: userId={}", userId);
            adminService.rejectInstructor(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "강사가 성공적으로 거절되었습니다.",
                    "userId", userId
            ));
        } catch (IllegalArgumentException e) {
            log.warn("강사 거절 실패 - 잘못된 요청: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "userId", userId
            ));
        } catch (IllegalStateException e) {
            log.warn("강사 거절 실패 - 상태 오류: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(409).body(Map.of( // 409 Conflict
                    "success", false,
                    "message", e.getMessage(),
                    "userId", userId
            ));
        } catch (Exception e) {
            log.error("강사 거절 중 서버 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 내부 오류가 발생했습니다.",
                    "userId", userId
            ));
        }
    }

    // ✅ 관리자 로그인 (반환 타입 수정)
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody LoginReqDto loginReqDto) {
        try {
            log.info("관리자 로그인 시도: {}", loginReqDto.getEmail());
            Map<String, String> tokens = adminService.adminLogin(loginReqDto);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.warn("관리자 로그인 실패: {}", loginReqDto.getEmail(), e);
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    // ✅ 카테고리 전체 조회 (GET 메서드 명시)
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            log.info("🔍 전체 카테고리 조회 요청 받음");
            List<Category> categories = categoryService.getAllCategories();
            log.info("🔍 전체 카테고리 조회 성공: {}개", categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("❌ 카테고리 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    // ✅ 카테고리 생성
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody CategoryReqDto dto) {
        try {
            log.info("카테고리 생성 요청: name={}", dto.getName());
            Category saved = categoryService.createCategory(dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.warn("카테고리 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("카테고리 생성 중 서버 오류", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "서버 에러 발생: " + e.getMessage()));
        }
    }

    // ✅ 카테고리 수정
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryReqDto dto) {
        try {
            log.info("카테고리 수정 요청: id={}, name={}", id, dto.getName());
            Category updated = categoryService.updateCategory(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("카테고리 수정 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("카테고리 수정 중 서버 오류: id={}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("message", "서버 에러 발생: " + e.getMessage()));
        }
    }

    // ✅ 카테고리 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            log.info("카테고리 삭제 요청: id={}", id);
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "카테고리가 성공적으로 삭제되었습니다.",
                    "id", id
            ));
        } catch (IllegalArgumentException e) {
            log.warn("카테고리 삭제 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("카테고리 삭제 실패 - 상태 오류: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("카테고리 삭제 중 서버 오류: id={}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("message", "서버 에러 발생: " + e.getMessage()));
        }
    }

    // ✅ 카테고리 상태 토글
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/categories/{id}/toggle")
    public ResponseEntity<?> toggleCategoryStatus(@PathVariable Long id) {
        try {
            log.info("카테고리 상태 토글 요청: id={}", id);
            Category updated = categoryService.toggleCategoryStatus(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("카테고리 상태 토글 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("카테고리 상태 토글 중 서버 오류: id={}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("message", "서버 에러 발생: " + e.getMessage()));
        }
    }
}
