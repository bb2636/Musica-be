package com.example.musica_be.controller;

import com.example.musica_be.config.KakaoConfig;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.service.user.UserService;
import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SocialAuthController {

    @Autowired
    private KakaoConfig kakaoConfig;

    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    // 카카오 로그인 콜백 처리
    @GetMapping("/login/oauth2/code/kakao")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) {
        try {
            // 1. 인가 코드로 카카오에 액세스 토큰 요청
            String tokenUri = "https://kauth.kakao.com/oauth/token";
            String body = "grant_type=authorization_code&client_id=" + kakaoConfig.getClientId() +
                    "&redirect_uri=" + kakaoConfig.getRedirectUri() + "&code=" + code;

            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    String.class
            );

            String accessToken = parseAccessToken(response.getBody());

            // 2. 카카오 사용자 정보 요청
            String userInfoUri = "https://kapi.kakao.com/v2/user/me";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            String email = parseEmail(userInfoResponse.getBody());
            String name = parseName(userInfoResponse.getBody());
            String kakaoId = parseKakaoId(userInfoResponse.getBody());

            // 3. 기존 소셜 계정 찾기
            Optional<User> user = userService.findByEmail(email);

            // 4. 사용자 없으면 새로 생성, 있으면 소셜 계정 연결
            if (user.isEmpty()) {
                user = Optional.of(new User(email, name, Role.USER, LocalDateTime.now(), new BCryptPasswordEncoder().encode("defaultPassword")));
                userService.save(user.get());
            }

            // 5. 소셜 계정 연결
            userService.connectSocialAccount(kakaoId, "kakao", user.get());

            // 6. JWT 생성 및 응답 (role을 USER로 설정)
            String accessTokenJwt = JwtUtils.generateAccessToken(user.get().getEmail(), kakaoId, "USER");
            String refreshToken = JwtUtils.generateRefreshToken(user.get().getEmail(), kakaoId, "USER");

            return ResponseEntity.ok("redirect:/user/signup?email=" + email + "&name=" + name + "&role=USER");

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error during Kakao login");
        }
    }


    // 회원가입 페이지에서 'role'과 'level'을 선택하고 최종적으로 DB에 저장
    @PostMapping("/user/signup")
    public ResponseEntity<UserResDto> completeSignup(@RequestParam String email,
                                                     @RequestParam String name,
                                                     @RequestParam String role,
                                                     @RequestParam(required = false) Long levelId) {
        try {
            UserResDto userResDto = userService.registerUserFromOAuth(email, name, role, levelId);
            return ResponseEntity.ok(userResDto);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new UserResDto(e.getMessage()));
        }
    }

    // 토큰 파싱 예시
    private String parseAccessToken(String response) {
        // 토큰 파싱 로직 (예: JSON 파싱)
        return "accessToken";
    }

    private String parseEmail(String response) {
        // 이메일 파싱 로직
        return "email";
    }

    private String parseName(String response) {
        // 이름 파싱 로직
        return "name";
    }

    private String parseKakaoId(String response) {
        // 카카오 ID 파싱 로직
        return "kakaoId";
    }
}
