package com.example.musica_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public class KakaoConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}