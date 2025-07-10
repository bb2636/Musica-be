package com.example.musica_be.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test-jwt")
public class JwtGenerator {

    @GetMapping
    public ResponseEntity<?> getToken(
        @RequestParam String email,
        @RequestParam String userId
    ) {
        String token = JwtUtils.generateAccessToken(email, userId);
        return ResponseEntity.ok(Map.of("token", token, "message", "JWT 토큰 생성 완료"));
    }
}
