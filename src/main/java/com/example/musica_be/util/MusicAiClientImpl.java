package com.example.musica_be.util;

import com.example.musica_be.dto.instrumentAnalysis.JobCreateRequestDto;
import com.example.musica_be.dto.instrumentAnalysis.JobCreateResponseDto;
import com.example.musica_be.dto.instrumentAnalysis.JobStatusResponseDto;
import com.example.musica_be.dto.instrumentAnalysis.MusicAiJobRequestDto;
import com.example.musica_be.repository.instrumentAnalysis.MusicAiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class MusicAiClientImpl implements MusicAiClient {
    // Spring 의 HTTP 통신 도구
    // 외부 API 와의 요청, 응답 처리에 사용됨
    private final RestTemplate restTemplate;

    @Value("${musicai.api.key}")
    private String apiKey;

    @Value("${musicai.api.base-url}")
    private String baseUrl;

    // Music.ai 워크플로에 Job 생성 요청(영상 분석 요청)을 보내고 응답으로 Job 아이디를 받음
    /**
     * Music.AI Job 생성 요청
     *
     * 외부 Music.AI API에 분석 작업(Job)을 생성하기 위한 POST 요청을 전송합니다.
     *
     * 내부 시스템에서 사용하는 {@link JobCreateRequestDto}를 외부 API 스펙에 맞는
     * {@link MusicAiJobRequestDto}로 변환하여 요청 본문에 포함하고,
     * 성공적으로 생성되면 Job ID 등의 정보가 담긴 응답을 반환합니다.
     *
     * 요청 시 사용되는 주요 정보:
     * - jobName: 고유 작업 이름
     * - workflowSlug: 실행할 워크플로 이름
     * - videoUrl: 분석할 S3 동영상 URL
     *
     * @param req 내부 시스템용 Job 생성 요청 DTO
     * @return 생성된 Job 정보(ID 등)를 포함하는 응답 DTO
     */
    @Override
    public JobCreateResponseDto createJob(JobCreateRequestDto req) {
        // 기존 내부 DTO → Music.AI 요청 DTO 변환
        MusicAiJobRequestDto requestDto = new MusicAiJobRequestDto(
            req.getJobName(),
            req.getWorkflowSlug(),
            new MusicAiJobRequestDto.Params(req.getParams().getVideoUrl())
        );

        String url = baseUrl + "/v1/job";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey); // Bearer 접두사 없이 API 키만 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MusicAiJobRequestDto> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<JobCreateResponseDto> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            JobCreateResponseDto.class
        );

        return response.getBody(); // 응답 예시: {"id":"a913v240-7ce1-3571-a22q-f4q3cdabcd1e"}
    }

    /**
     * Music.AI 분석 결과 조회
     *
     * 외부 Music.AI API로부터 특정 Job의 현재 상태 및 분석 결과를 조회합니다.
     *
     * 1. 주어진 Job ID로 GET 요청을 전송하여 전체 응답(JSON)을 문자열로 수신
     * 2. 응답을 {@link JobStatusResponseDto}로 역직렬화
     * 3. 응답 내부의 result.resultJson 필드에 포함된 악기 분석 결과를 다시 한 번 역직렬화
     * 4. 최종적으로 Detection 정보를 DTO에 주입하여 반환
     *
     * 분석이 완료되지 않았거나, 내부 resultJson이 null인 경우에는 예외가 발생할 수 있습니다.
     *
     * @param jobId 분석할 Music.AI Job의 고유 식별자
     * @return 분석 상태 및 감지 결과가 포함된 DTO 객체
     * @throws RuntimeException JSON 파싱 실패 시 예외 발생
     */
    @Override
    public JobStatusResponseDto getJobResult(String jobId) {
        String url = baseUrl + "/v1/job/" + jobId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey); // Bearer 없이 API 키만
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            String.class
        );

        String rawJson = response.getBody();
        log.info("응답 원본 JSON: {}", rawJson);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules(); // JavaTimeModule 포함

            JobStatusResponseDto dto = mapper.readValue(rawJson, JobStatusResponseDto.class);

            String resultJson = dto.getResult().getResultJson();
            JobStatusResponseDto.Detection detection = mapper.readValue(resultJson, JobStatusResponseDto.Detection.class);

            // 파싱한 감지 결과를 DTO에 주입
            dto.setDetection(detection);

            log.info("감지된 악기: {}", detection.getInstruments());

            return dto;

        } catch (Exception e) {
            log.error("JSON 파싱 실패", e);
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}
