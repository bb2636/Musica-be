package com.example.musica_be.dto.instrumentAnalysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class JobCreateRequestDto {
    private String jobName;       // job 이름
    private String workflowSlug;  // workflow 의 slug 값
    private JobParams params;     // 내부 중첩 객체

    @Getter
    @AllArgsConstructor
    @ToString
    public static class JobParams {
        @JsonProperty("VideoUrl")
        private String videoUrl;  // downloadUrl 값 (워크플로우의 Input 부분과 일치), 실제 분석할 파일의 다운로드 URL
    }
}
