package com.example.musica_be.dto.instrumentAnalysis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Music.AI API에서 Job 상태 및 결과를 받아오는 DTO.
 * 중첩된 JSON 구조와 문자열 파싱이 필요한 result(ResultWrapper) 외에도,
 * 외부 API에서 내려주는 추가 필드를 수용할 수 있도록 설계함.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드는 무시
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JobStatusResponseDto {

    // 고유 Job 식별자
    private String id;

    // 현재 Job 상태 (예: SUCCEEDED, RUNNING 등)
    private String status;

    // 작업 완료 시각 (ISO-8601 포맷)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private LocalDateTime completedAt;

    // 중첩된 JSON 문자열이 포함된 result 래퍼 객체
    private ResultWrapper result;

    // 파싱된 감지 결과 (result 내부 JSON 문자열 파싱 결과)
    @Setter
    private Detection detection;

    // 추가 정보 (필요에 따라 응답에 포함될 수 있음)
    private String batchName;
    private String name;
    private String app;
    private String workflow;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private LocalDateTime createdAt;

    // 워크플로 파라미터 (중첩 구조 - 구조를 알면 DTO로 따로 빼는 게 좋음)
    private Object workflowParams;

    // ---------------- 내부 클래스 정의 ----------------

    /**
     * result 필드 내부에 중첩되어 있는 "Result" JSON 문자열을 감싸는 클래스.
     * 문자열로 들어온 감지 결과를 다시 한 번 수동 파싱해야 함.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ResultWrapper {
        @JsonProperty("Result") // 정확히 대소문자 일치해야 함
        private String resultJson;
    }

    /**
     * 실제 감지된 악기 정보 (result.Result 내부의 JSON 구조)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Detection {

        // 감지된 악기 여부 (true/false)
        private Map<String, Boolean> instruments;

        // 각 악기의 감지 확률
        private Map<String, Double> probabilities;

        // 각 악기의 감지 기준 임계값
        private Map<String, Double> thresholds;
    }
}