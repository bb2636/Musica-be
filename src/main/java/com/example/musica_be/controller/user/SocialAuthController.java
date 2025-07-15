package com.example.musica_be.controller.user;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.user.SocialSignupReqDto;
import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SocialAuthController {

    private final UserService userService;

    // ✅ 카카오 로그인 이후 추가 정보 등록 (role + levelId)
    @PostMapping("/user/signup")
    public ResponseEntity<UserResDto> kakaoSignup(@RequestBody SocialSignupReqDto dto) {
        try {
            // ✅ 서비스는 User 객체만 반환
            User user = userService.registerUserFromOAuth(
                    dto.getEmail(),
                    dto.getName(),
                    dto.getRole(),
                    dto.getLevelId()
            );

            return ResponseEntity.ok(new UserResDto(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new UserResDto(e.getMessage()));
        }
    }
}
