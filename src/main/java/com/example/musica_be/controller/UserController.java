package com.example.musica_be.controller;

import com.example.musica_be.service.user.UserService;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.dto.user.RegisterReqDto;
import com.example.musica_be.dto.user.UserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원가입
    @PostMapping("/users/register")
    public ResponseEntity<UserResDto> registerUser(@RequestBody RegisterReqDto registerReqDto) {
        try {
            UserResDto userResDto = userService.registerUser(registerReqDto);
            return ResponseEntity.ok(userResDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginReqDto loginReqDto) {
        try {
            Map<String, String> tokens = userService.login(loginReqDto);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage())); // 로그인 실패 시 메시지 반환
        }
    }

    // 로그아웃 (Optional - 서버 측 처리)
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout() {
        try {
            // 로그아웃 시 서버 측에서 처리할 내용 (예: 세션 무효화 등)
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }
}
