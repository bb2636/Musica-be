package com.example.musica_be.security;

import com.example.musica_be.util.JwtUtils;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    // 리프레시 토큰을 사용하여 액세스 토큰을 갱신
    public String refreshAccessToken(String refreshToken) {
        return JwtUtils.refreshAccessToken(refreshToken);
    }

    // 액세스 토큰 발급
    public String generateAccessToken(String email, String userId) {
        return JwtUtils.generateAccessToken(email, userId);
    }

    // 리프레시 토큰 발급
    public String generateRefreshToken(String email, String userId) {
        return JwtUtils.generateRefreshToken(email, userId);
    }
}
