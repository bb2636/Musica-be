package com.example.musica_be.controller.user;

import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.service.user.AdminService;
import com.example.musica_be.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
}