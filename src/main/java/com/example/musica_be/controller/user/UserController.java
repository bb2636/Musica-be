package com.example.musica_be.controller.user;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.Wishlist;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.question.QuestionDto;
import com.example.musica_be.dto.user.*;
import com.example.musica_be.security.JwtTokenService;
import com.example.musica_be.service.user.BlacklistService;
import com.example.musica_be.service.user.UserService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final BlacklistService blacklistService;

    // 회원가입
    @PostMapping("/users/register")
    public ResponseEntity<UserResDto> registerUser(@RequestBody RegisterReqDto registerReqDto) {
        try {
            UserResDto userResDto = userService.registerUser(registerReqDto);
            return ResponseEntity.ok(userResDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new UserResDto(e.getMessage()));
        }
    }
    //회원가입 - 이메일 중복확인
    //ex) {{domain}}/api/users/check-email?email=test@test.com
    @GetMapping("/users/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        boolean exists = userService.isEmailDuplicate(email);
        return ResponseEntity.ok(exists); // true면 중복
    }

    // 일반 로그인 (사용자, 강사 로그인)
    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginReqDto loginReqDto) {
        try {
            Map<String, String> tokens = userService.login(loginReqDto);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage())); // 로그인 실패 시 메시지 반환
        }
    }

    // Refresh Token 기반으로 AccessToken 재발급
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // ✅ DB에 존재하는지 확인
            if (!userService.existsByRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "DB에 등록되지 않은 RefreshToken"));
            }

            String status = JwtUtils.validateAndGetStatus(refreshToken);
            if ("INVALID".equals(status)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "유효하지 않은 토큰입니다"));
            } else if ("EXPIRED".equals(status)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Refresh 토큰이 만료되었습니다"));
            }

            String newAccessToken = JwtUtils.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(new TokenResponse(newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "토큰 재발급 오류: " + e.getMessage()));
        }
    }

    // 로그아웃 (Optional - 서버 측 처리)
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout(
            @AuthenticationPrincipal User user,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            // ✅ 1. AccessToken 블랙리스트에 추가
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String accessToken = authorizationHeader.substring(7);
                // JWT에서 만료시간 파싱
                Date expiryDate = JwtUtils.getExpirationFromToken(accessToken);
                LocalDateTime expiryLocalDateTime = expiryDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                blacklistService.blacklistAccessToken(accessToken, expiryLocalDateTime);
                log.info("AccessToken blacklisted: {}", accessToken);
            }

            // ✅ 2. RefreshToken DB에서 삭제
            userService.deleteByUser(user);
            log.info("Deleted RefreshToken for userId: {}", user.getId());

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return ResponseEntity.status(400).body("Logout error: " + e.getMessage());
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
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserResDto> updateUser(
        @PathVariable Long userId,
        @RequestBody UpdateUserReqDto updateUserReqDto) {
        try {
            UserResDto updatedUser = userService.updateUserInfo(userId, updateUserReqDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserResDto(e.getMessage()));
        }
    }

    //사용자 마이페이지 정보 조회
    @PreAuthorize("hasAnyRole('USER', 'INSTRUCTOR', 'ADMIN')")
    @GetMapping("/users/mypage")
    public ResponseEntity<UserResDto> getUserMypage(@AuthenticationPrincipal User user) {
        // 사용자 정보 가져오기
        UserResDto userResDto = new UserResDto(user);
        return ResponseEntity.ok(userResDto);
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users/mypage/questions")
    public ResponseEntity<List<QuestionDto>> getMyQuestions(
        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserQuestions(user.getId()));
    }
}