package com.example.musica_be.config;

import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.service.user.UserService;
import com.example.musica_be.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        // ✅ 카카오 정보 추출 (중첩 구조)
        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        Map<String, Object> properties = oAuth2User.getAttribute("properties");

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String name = properties != null ? (String) properties.get("nickname") : "unknown";
        Long id = (Long) oAuth2User.getAttribute("id");
        String kakaoId = String.valueOf(id);

        // ✅ 디버깅 로그
        System.out.println("✅ 카카오 로그인 정보: email=" + email + ", nickname=" + name + ", id=" + kakaoId);

        // 사용자 등록 또는 찾기
        User user = userService.findByEmail(email)
                .orElseGet(() -> {
                    // Level id=1 기본값 주입
                    var defaultLevel = userService.getLevelById(1L);

                    User newUser = new User(email, name, Role.USER, LocalDateTime.now(), "default");
                    newUser.setLevel(defaultLevel);
                    newUser.setIsApproved(true); // 카카오는 자동 승인

                    return userService.save(newUser);
                });

        userService.connectSocialAccount(kakaoId, "kakao", user);

        // JWT 생성
        String token = JwtUtils.generateAccessToken(user.getEmail(), kakaoId, "USER");

        // 리디렉션 (프론트에서 token 파싱)
        String redirectUrl = "http://localhost:5173/oauth-success?token=" + token;
        System.out.println("✅ 리디렉션 URL: " + redirectUrl);
        response.sendRedirect(redirectUrl);
        System.out.println("✅ OAuth 로그인 성공: " + email);
    }
}


