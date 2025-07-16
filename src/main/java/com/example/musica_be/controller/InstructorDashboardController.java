package com.example.musica_be.controller;

import com.example.musica_be.dto.instructor.InstructorDashboardResDto;
import com.example.musica_be.service.instructor.InstructorDashboardService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instructors")
public class InstructorDashboardController {

    private final InstructorDashboardService InstructorDashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<InstructorDashboardResDto> getInstructorDashboard(
        @RequestHeader("Authorization") String jwt
    ) {
        Long userId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(InstructorDashboardService.getInstructorDashboard(userId));
    }
}
