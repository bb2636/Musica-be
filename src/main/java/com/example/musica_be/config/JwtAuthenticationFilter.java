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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        // ✅ 블랙리스트에 있는지 체크
        if (blacklistService.isBlacklisted(token)) {
            log.warn("Access token is blacklisted: {}", token);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "This token is blacklisted.");
            return;
        }

        try {
            String userIdStr = JwtUtils.getUserIdFromToken(token);
            Long userId = Long.valueOf(userIdStr);

            // User 엔티티 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            log.info("userId: {}", userId);
            System.out.println("userId from jwt: " + userId);

            // ✅ role 정보도 JWT에서 파싱
            String role = JwtUtils.getRoleFromToken(token);

            // ✅ 권한 정보를 포함한 인증 객체 생성
            // 스프링 시큐리티는 내부적으로 "ROLE_" 접두사가 붙은 권한을 사용함
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // ✅ 사용자 ID + 권한으로 인증 객체 생성
            // 이렇게 해야 hasRole("INSTRUCTOR") 등의 인가 설정이 정상 작동함
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, authorities);

            log.info("🔐 필터 들어옴: {}", request.getRequestURI()); // todo: 로그 코드 추가 - 강동균
            log.info("🔐 토큰: {}", token); // todo: 로그 코드 추가 - 강동균
            log.info("🔐 userId: {}, role: {}", userId, role); // todo: 로그 코드 추가 - 강동균

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Token");
            return;
        }

        chain.doFilter(request, response);
    }
}