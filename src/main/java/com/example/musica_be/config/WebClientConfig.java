package com.example.musica_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
public class WebClientConfig {

    @Value("${openai.api.key}")
    private String openAiApiKey;
    @Bean(name = "openAiWebClient")
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Value("${toss.api.key}")
    private String  tossApiKey;
    @Bean(name = "tossWebClient")
    public WebClient tossWebClient() {
        tossApiKey = Base64.getEncoder().encodeToString((tossApiKey+":").getBytes());
        return WebClient.builder()
            .baseUrl("https://api.tosspayments.com/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic "+tossApiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
