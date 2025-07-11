package com.example.musica_be.config;

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
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        // 원래 코드
        // todo: gpt 에게 요청하여 주석 추가
//        try {
//            String userId = JwtUtils.getUserIdFromToken(token);
//            log.info("userId: {}", userId);
//            System.out.println("userId from jwt: " + userId);
//
//            // ✅ 사용자 ID만으로 인증 객체 생성 (권한 정보 없음)
//            // ❗ 이 상태로는 hasRole, hasAuthority를 사용하는 인가 검증을 통과할 수 없음
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                userId, null, null); // 🔴 권한(authorities) = null
//
//            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        } catch (Exception ex) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Token");
//            return;
//        }

        // todo: 추가한 코드 - 강동균 (2025. 07. 10. 23:07)
        // todo: 권한 기반 인증에서는 이 코드를 사용해야 함 (403 에러 발생)
        try {
            String userId = JwtUtils.getUserIdFromToken(token);
            log.info("userId: {}", userId);
            System.out.println("userId from jwt: " + userId);

            // ✅ role 정보도 JWT에서 파싱
            String role = JwtUtils.getRoleFromToken(token);
            log.info("role: {}", role);
            System.out.println("role from jwt: " + role);

            // ✅ 권한 정보를 포함한 인증 객체 생성
            // 스프링 시큐리티는 내부적으로 "ROLE_" 접두사가 붙은 권한을 사용함
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            log.info("authorities: {}", authorities);
            System.out.println("authorities = " + authorities);

            // ✅ 사용자 ID + 권한으로 인증 객체 생성
            // 이렇게 해야 hasRole("INSTRUCTOR") 등의 인가 설정이 정상 작동함
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Token");
            return;
        }

        chain.doFilter(request, response);
    }
}