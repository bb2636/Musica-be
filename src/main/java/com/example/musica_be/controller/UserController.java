package com.example.musica_be.controller;

import com.example.musica_be.dto.user.UpdateUserReqDto;
import com.example.musica_be.service.user.UserService;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.dto.user.RegisterReqDto;
import com.example.musica_be.dto.user.UserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            // 예외 처리 시 UserResDto로 감싸서 반환
            return ResponseEntity.badRequest().body(new UserResDto(e.getMessage()));
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

    // 회원 정보 수정
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserResDto> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserReqDto updateUserReqDto) {  // RequestBody로 데이터 받기
        try {
            UserResDto updatedUser = userService.updateUser(userId, updateUserReqDto); // DTO를 전달
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserResDto(e.getMessage()));
        }
    }
}