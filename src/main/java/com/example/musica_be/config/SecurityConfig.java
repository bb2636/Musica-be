package com.example.musica_be.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // todo: 추가한 코드 - 강동균 (2025. 07. 10. 23:36) - 생성자 주입을 위한 의존성 필드 및 생성자 추가
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
                .authorizeHttpRequests( auth -> auth
                .requestMatchers(
                        "/api/users/register",
                        "/api/auth/login",
                        "/api/admin/login",
                        "/api/users/**",
                        "/api/dev/**", //개발용 임의 데이터 삽입 컨트롤러
                        "/api/reviews/summary/lecture/**", //후기 요약 ai
                        "/api/users/check-email", //회원가입 시 이메일 중복 체크
                        "/api/levels", //회원가입 시 레벨 테이블 불러오기
                        "/api/reviews/classes/**", //클래스 별 후기 전체 조회
                        "/oauth2/**",
                        "/oauth2/authorization/kakao", //카카오 로그인 용
                        "/login/oauth2/**", //리다이렉션 url 허용
                        "/oauth-success", // 프론트 리디렉션 용
                        "/api/user/signup", //카카오 회원가입 시 롤, 레벨 입력받는 컨트롤러
                        "/api/payment/cart/checkout"// 카트 결제 (토스 결제를 위한)

                ).permitAll()
                // URL 경로를 역할별로 나눔 // todo: 추가한 코드 - 강동균 (2025. 07. 11. 00:02)
                .requestMatchers("/api/users/**","/api/auth/**").hasRole("USER") // todo: 추가한 코드 - 강동균 (2025. 07. 11. 00:02)
                .requestMatchers("/api/instructors/**").hasRole("INSTRUCTOR") // todo: 추가한 코드 - 강동균 (2025. 07. 11. 00:02)
                .requestMatchers("/api/admins/**").hasRole("ADMIN") // todo: 추가한 코드 - 강동균 (2025. 07. 11. 00:02)
//                .requestMatchers("/api/**").hasRole("USER") // todo: 주석 처리 - 강동균 (2025. 07. 11. 00:01)
//                .requestMatchers("/admin/**").hasRole("ADMIN") // todo: 주석 처리 - 강동균 (2025. 07. 11. 00:01)
//                .requestMatchers("/instructor/**").hasRole("INSTRUCTOR") // todo: 주석 처리 - 강동균 (2025. 07. 11. 00:01)
                .anyRequest().authenticated()
                )
                //인증 필요없는 요청 보낼 때 카카오 리디렉션 막기
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Unauthorized: Please provide valid JWT token");
                        })
                )
                // ✅ JWT 인증 필터 등록 (세션 방식과 독립적으로 작동)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 원래 코드
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // todo: 수정한 코드 - 강동균 (2025. 07. 10. 23:35) → 빈 주입 방식

                // ✅ 소셜 로그인 설정 추가 (카카오) 기존 로직과 별개로 작동하기 위해 분리
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customOAuth2SuccessHandler)
                );

        return http.build();
    }
//    @Bean
//    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
//        if (kakaoConfig.getClientId() == null || kakaoConfig.getClientId().isEmpty()) {
//            throw new IllegalStateException("Kakao clientId cannot be empty");
//        }
//
//        ClientRegistration kakao = ClientRegistration.withRegistrationId("kakao")
//                .clientId(kakaoConfig.getClientId())
//                .clientSecret(kakaoConfig.getClientSecret())
//                .redirectUri("{baseUrl}/api/auth/login/oauth2/code/{registrationId}")
//                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
//                .tokenUri("https://kauth.kakao.com/oauth/token")
//                .userInfoUri("https://kapi.kakao.com/v2/user/me")
//                .userNameAttributeName("id")
//                .clientName("Kakao")
//                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//                .build();
//
//        return new InMemoryClientRegistrationRepository(kakao);
//    }
}

