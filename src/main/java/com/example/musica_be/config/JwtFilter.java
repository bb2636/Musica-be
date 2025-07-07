package com.example.musica_be.config;

import io.jsonwebtoken.JwtException;
import com.example.musica_be.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;  // jakarta.servlet 패키지 사용
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    // JwtFilter 생성자
    public JwtFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // doFilterInternal 메서드 구현
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        // Bearer Token인 경우 처리
        if (token != null && token.startsWith("Bearer ")) {
            try {
                // JWT 토큰에서 이메일 추출
                String email = JwtUtils.getEmailFromToken(token.substring(7));  // 'Bearer ' 제거
                if (email != null) {
                    // 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    // 인증 정보를 SecurityContext에 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException e) {
                // 토큰이 유효하지 않거나 만료된 경우
                SecurityContextHolder.clearContext();
            }
        }

        // 후속 필터 실행
        filterChain.doFilter(request, response);
    }
}
