package com.example.musica_be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                //.allowedOrigins("http://musica.o-r.kr")
                //.allowedOrigins("*") //임시로 모든 도메인 허용
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true); // JWT 쿠키 등 인증정보 포함 허용
    }
}
