package com.example.musica_be.util;

import com.example.musica_be.dto.instrumentAnalysis.JobCreateRequestDto;
import com.example.musica_be.dto.instrumentAnalysis.JobCreateResponseDto;
import com.example.musica_be.dto.instrumentAnalysis.UrlResponseDto;
import com.example.musica_be.repository.instrumentAnalysis.MusicAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class MusicAiClientImpl implements MusicAiClient {

    // Spring 의 HTTP 통신 도구
    // 외부 API 와의 요청, 응답 처리에 사용됨
    private final RestTemplate restTemplate;

    @Value("${spring.musicai.api.key}")
    private String apiKey;

    @Value("${spring.musicai.api.base-url}")
    private String baseUrl;

    // Presigned URL 발급 요청을 보내고 응답으로 uploadUrl 과 downloadUrl 을 받음
    @Override
    public UrlResponseDto getUrl() {
        // https://api.music.ai/v1/upload
        String url = baseUrl + "/v1/upload";

        // headers
        HttpHeaders headers = new HttpHeaders();
        // Authorization: your-api-key
        headers.setBearerAuth(apiKey);
        // Content-Type: application/json
        headers.setContentType(MediaType.APPLICATION_JSON); // TODO: 꼭 필요한지 테스트 - 주석 처리해도 되는지?

        // 인증과 JSON 헤더를 포함한 요청 객체 구성
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<UrlResponseDto> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            UrlResponseDto.class
        );

        return response.getBody();
    }

    // Music.ai 워크플로에 Job 생성 요청(영상 분석 요청)을 보내고 응답으로 Job 아이디를 받음
    @Override
    public JobCreateResponseDto createJob(JobCreateRequestDto req) {
        // https://api.music.ai/v1/job
        String url = baseUrl + "/v1/job";

        // headers
        HttpHeaders headers = new HttpHeaders();
        // Authorization: your-api-key
        headers.setBearerAuth(apiKey);
        // Content-Type: application/json
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 분석 요청에 필요한 데이터를 DTO 형태로 전송
        HttpEntity<JobCreateRequestDto> request = new HttpEntity<>(req, headers);

        ResponseEntity<JobCreateResponseDto> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            JobCreateResponseDto.class
        );

        return response.getBody(); // 응답 예시: {"id":"a913v240-7ce1-3571-a22q-f4q3cdabcd1e"}
    }
}
