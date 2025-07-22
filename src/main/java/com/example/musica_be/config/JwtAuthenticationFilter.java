package com.example.musica_be.config;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.service.user.BlacklistService;
import com.example.musica_be.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final BlacklistService blacklistService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || "Bearer null".equals(header)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // ✅ 블랙리스트 체크
        if (blacklistService.isBlacklisted(token)) {
            log.warn("🚫 블랙리스트된 토큰: {}", token.substring(0, 20) + "...");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Blacklisted token\"}");
            return;
        }

        try {
            // ✅ JWT에서 사용자 정보 추출
            String userIdStr = JwtUtils.getUserIdFromToken(token);
            Long userId = Long.valueOf(userIdStr);
            String role = JwtUtils.getRoleFromToken(token);
            String email = JwtUtils.getEmailFromToken(token);

            // ✅ 사용자 존재 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            // ✅ 권한 정보 생성 (ROLE_ 접두사 추가)
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

            // ✅ 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ 디버그 로그
            log.info("🔐 JWT 인증 성공 - URI: {}, userId: {}, email: {}, role: {}, authorities: {}",
                    request.getRequestURI(), userId, email, role, authorities);

        } catch (Exception ex) {
            log.error("🚫 JWT 인증 실패 - URI: {}, error: {}",
                    request.getRequestURI(), ex.getMessage());

            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Invalid JWT token\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}