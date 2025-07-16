package com.example.musica_be.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ✅ @PreAuthorize 활성화
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Lazy CustomOAuth2SuccessHandler customOAuth2SuccessHandler,
            CustomOAuth2UserService customOAuth2UserService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                org.springframework.security.core.userdetails.User.withUsername("user")
                        .password(passwordEncoder().encode("password"))
                        .roles("USER")
                        .build(),
                org.springframework.security.core.userdetails.User.withUsername("admin")
                        .password(passwordEncoder().encode("admin"))
                        .roles("ADMIN")
                        .build(),
                org.springframework.security.core.userdetails.User.withUsername("instructor")
                        .password(passwordEncoder().encode("instructorPassword"))
                        .roles("INSTRUCTOR")
                        .build()
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ✅ 공개 접근 허용
                        .requestMatchers(
                                "/api/users/register",
                                "/api/auth/login",
                                "/api/auth/refresh",  // ✅ refresh 토큰 엔드포인트 추가
                                "/api/admin/login",  // 관리자 로그인은 공개
                                "/api/dev/**", // 개발용 임의 데이터 삽입 컨트롤러
                                "/api/reviews/summary/lecture/**", // 후기 요약 AI
                                "/api/users/check-email", // 회원가입 시 이메일 중복 체크
                                "/api/levels", // 회원가입 시 레벨 테이블 불러오기
                                "/api/reviews/classes/**", // 클래스 별 후기 전체 조회
                                "/oauth2/**",
                                "/oauth2/authorization/kakao", // 카카오 로그인 용
                                "/login/oauth2/**", // 리다이렉션 URL 허용
                                "/oauth-success", // 프론트 리디렉션 용
                                "/api/user/signup", // 카카오 회원가입 시 롤, 레벨 입력받는 컨트롤러
                                "/api/payment/cart/checkout", // 카트 결제 (토스 결제를 위한)
                                "/api/main/**" // 메인페이지 (추천,인기,최신,후기요약 AI 등등)
                        ).permitAll()

                        // ✅ 구체적인 경로를 먼저 배치 (우선순위 높음)
                        .requestMatchers("/api/users/mypage").hasAnyRole("USER", "INSTRUCTOR", "ADMIN")

                        // ✅ 관리자 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ✅ 강사 전용
                        .requestMatchers("/api/instructors/**").hasRole("INSTRUCTOR")

                        // 메인페이지 클래스 추천 허용(GET)
                        .requestMatchers("/api/classes/recommend").hasRole("USER")
                        // 클래스 상세 조회 허용 (GET)
                        .requestMatchers(HttpMethod.GET, "/api/classes/**").permitAll()
                        // 클래스 검색 허용 (GET)
                        .requestMatchers(HttpMethod.GET, "/api/classes").permitAll()
                        // 강의 목록 조회 허용 (GET)
                        .requestMatchers(HttpMethod.GET, "/api/classes/*/lectures").permitAll()

                        // ✅ 6. 나머지 사용자 API
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "INSTRUCTOR", "ADMIN")
                        .requestMatchers("/api/auth/**").hasAnyRole("USER", "INSTRUCTOR", "ADMIN")

                        .anyRequest().authenticated()
                )
                // ✅ 예외 처리 개선
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("🔒 인증 실패: {} - {}", request.getRequestURI(), authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("🚫 권한 부족: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"Forbidden\",\"message\":\"권한이 부족합니다.\"}"
                            );
                        })
                )
                // ✅ JWT 인증 필터 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ 소셜 로그인 설정 추가 (카카오)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customOAuth2SuccessHandler)
                );

        return http.build();
    }
}