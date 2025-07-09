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
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private KakaoConfig kakaoConfig;

    // PasswordEncoder 빈 등록 (BCrypt 암호화 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 사용자 정보 서비스 (InMemoryUserDetailsManager를 사용하거나, 사용자 DB에서 조회하도록 구현할 수 있습니다)
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

    // AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    // Spring Security 6.x 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/users/register", "/api/auth/login", "/api/admin/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/instructor/**").hasRole("INSTRUCTOR")
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .clientRegistrationRepository(clientRegistrationRepository())
                .defaultSuccessUrl("/user/home", true)
                .failureUrl("/api/auth/login?error=true");
        return http.build();
    }

    // 카카오 OAuth2 클라이언트 설정
    @Bean
    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
        if (kakaoConfig.getClientId() == null || kakaoConfig.getClientId().isEmpty()) {
            throw new IllegalStateException("Kakao clientId cannot be empty");
        }

        ClientRegistration kakaoRegistration = ClientRegistration.withRegistrationId("kakao")
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

        return new InMemoryClientRegistrationRepository(kakaoRegistration);  // 카카오 등록 정보를 InMemory 저장소에 등록
    }
}

