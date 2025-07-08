package com.example.musica_be.security;

import com.example.musica_be.util.JwtUtils;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    // 리프레시 토큰을 사용하여 액세스 토큰을 갱신
    public String refreshAccessToken(String refreshToken) {
        try {
            return JwtUtils.refreshAccessToken(refreshToken);
        } catch (Exception e) {
            // Add proper error handling here
            throw new RuntimeException("Error refreshing access token", e);
        }
    }

    // 액세스 토큰 발급
    public String generateAccessToken(String email, String userId, String role) {
        try {
            return JwtUtils.generateAccessToken(email, userId, role);
        } catch (Exception e) {
            // Add proper error handling here
            throw new RuntimeException("Error generating access token", e);
        }
    }

    // 리프레시 토큰 발급
    public String generateRefreshToken(String email, String userId, String role) {
        try {
            return JwtUtils.generateRefreshToken(email, userId, role);
        } catch (Exception e) {
            // Add proper error handling here
            throw new RuntimeException("Error generating refresh token", e);
        }
    }
}
