package com.example.musica_be.config;

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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        // ✅ 카카오 정보 추출
        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        Map<String, Object> properties = oAuth2User.getAttribute("properties");

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String name = properties != null ? (String) properties.get("nickname") : "unknown";
        Long id = (Long) oAuth2User.getAttribute("id");
        String kakaoId = String.valueOf(id);

        System.out.println("✅ 카카오 로그인 정보: email=" + email + ", nickname=" + name + ", id=" + kakaoId);

        // ✅ User DB에 저장은 하지 않음.
        // JWT만 발급 (role USER, email, kakaoId 넣기)
        String token = JwtUtils.generateAccessToken(email, kakaoId, "USER", name);

        String redirectUrl = "http://localhost:5173/oauth-success?token=" + token;
        System.out.println("✅ 리디렉션 URL: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
