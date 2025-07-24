package com.example.musica_be.config;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.user.UserResDto;
import com.example.musica_be.service.user.UserService;
import com.example.musica_be.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        Map<String, Object> properties = oAuth2User.getAttribute("properties");

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String name = properties != null ? (String) properties.get("nickname") : "unknown";
        String provider = oauthToken.getAuthorizedClientRegistrationId(); // e.g. "kakao"
        Object idObj = oAuth2User.getAttribute("id");
        String kakaoId = String.valueOf(idObj);

        System.out.println("✅ 카카오 로그인 정보: email=" + email + ", nickname=" + name + ", kakaoId=" + kakaoId);

        // 🔥 DB에서 User 찾기 (없으면 회원가입)
        User user = userService.findByEmail(email)
                .orElseGet(() -> {
                    userService.registerUserFromOAuth(email, name, "USER", 1L);
                    return userService.findByEmail(email)
                            .orElseThrow(() -> new IllegalStateException("등록 후 User를 찾을 수 없습니다."));
                });

        // 🔥 반드시 DB id를 JWT sub에 넣기
        String token = JwtUtils.generateAccessToken(email, String.valueOf(user.getId()), user.getRole().name(), name);

        String redirectUrl = "http://musica.o-r.kr/oauth-success?token=" + token;
        System.out.println("✅ 리디렉션 URL: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}

