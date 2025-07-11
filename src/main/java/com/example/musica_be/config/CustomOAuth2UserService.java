package com.example.musica_be.config;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        System.out.println("✅ [CustomOAuth2UserService] Kakao AccessToken = " + accessToken);

        try {
            OAuth2User user = super.loadUser(userRequest);
            System.out.println("✅ [CustomOAuth2UserService] Kakao user attributes = " + user.getAttributes());
            return user;
        } catch (OAuth2AuthenticationException e) {
            System.out.println("❌ [CustomOAuth2UserService] 사용자 정보 불러오기 실패: " + e.getError().getErrorCode());
            System.out.println("❌ [CustomOAuth2UserService] 메시지: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ [CustomOAuth2UserService] 예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
