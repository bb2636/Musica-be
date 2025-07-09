package com.example.musica_be.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private KakaoConfig kakaoConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // In-memory 사용자 등록 (테스트용)
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
            .csrf().disable()
            .authorizeHttpRequests()
            .requestMatchers(
                "/api/users/register",
                "/api/auth/login",
                "/api/admin/login",
                "/api/dev/**",
                "/api/**"
            ).permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/instructor/**").hasRole("INSTRUCTOR")
            .anyRequest().authenticated();

        // ✅ 주석 처리: 카카오 로그인 비활성화 (개발 중 테스트 목적)
        // .and()
        //     .oauth2Login()
        //     .clientRegistrationRepository(clientRegistrationRepository())
        //     .defaultSuccessUrl("/user/home", true)
        //     .failureUrl("/api/auth/login?error=true");

        return http.build();
    }

    // 카카오 OAuth2 설정 (운영 시 활성화)
    @Bean
    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
        if (kakaoConfig.getClientId() == null || kakaoConfig.getClientId().isEmpty()) {
            throw new IllegalStateException("Kakao clientId cannot be empty");
        }

        ClientRegistration kakao = ClientRegistration.withRegistrationId("kakao")
            .clientId(kakaoConfig.getClientId())
            .clientSecret(kakaoConfig.getClientSecret())
            .redirectUri(kakaoConfig.getRedirectUri())
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .tokenUri("https://kauth.kakao.com/oauth/token")
            .userInfoUri("https://kapi.kakao.com/v2/user/me")
            .userNameAttributeName("id")
            .clientName("Kakao")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .build();

        return new InMemoryClientRegistrationRepository(kakao);
    }
}
