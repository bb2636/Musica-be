package com.example.musica_be.controller.user;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.Wishlist;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.question.QuestionDto;
import com.example.musica_be.dto.user.UpdateUserReqDto;
import com.example.musica_be.security.JwtTokenService;
import com.example.musica_be.service.user.UserService;
import com.example.musica_be.dto.user.LoginReqDto;
import com.example.musica_be.dto.user.RegisterReqDto;
import com.example.musica_be.dto.user.UserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenService jwtTokenService;

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


    // 로그아웃 (Optional - 서버 측 처리)
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout() {
        try {
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
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users/mypage")
    public ResponseEntity<UserResDto> getUserMypage(@AuthenticationPrincipal User user) {
        // 사용자 정보 가져오기
        UserResDto userResDto = new UserResDto(user);
        return ResponseEntity.ok(userResDto);
    }

    //    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/users/mypage/enrollments")
//    public ResponseEntity<List<Enrollment>> getEnrollments(@AuthenticationPrincipal User user) {
//        List<Enrollment> enrollments = userService.getCurrentEnrollments(user.getId());
//        return ResponseEntity.ok(enrollments);
//    }
//
//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/users/mypage/payments")
//    public ResponseEntity<List<Payment>> getPaymentHistory(@AuthenticationPrincipal User user) {
//        List<Payment> payments = userService.getPaymentHistory(user.getId());
//        return ResponseEntity.ok(payments);
//    }
//    //찜목록 조회
//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/users/mypage/wishlist")
//    public ResponseEntity<List<Wishlist>> getWishlist(@AuthenticationPrincipal User user) {
//        List<Wishlist> wishlist = userService.getWishlist(user.getId());
//        return ResponseEntity.ok(wishlist);
//    }
//    //후기 목록 조회
//    @PreAuthorize("hasRole('USER')")
//    @GetMapping("/users/mypage/reviews")
//    public ResponseEntity<List<Review>> getReviews(@AuthenticationPrincipal User user) {
//        List<Review> reviews = userService.getReviews(user.getId());
//        return ResponseEntity.ok(reviews);
//    }
//
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users/mypage/questions")
    public ResponseEntity<List<QuestionDto>> getMyQuestions(
        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserQuestions(user.getId()));
    }
}