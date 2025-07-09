package com.example.musica_be.dto.instrumentAnalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobCreateRequestDto {
    private String jobName;       // job 이름
    private String workflowSlug;  // workflow 의 slug 값
    private JobParams params;     // 내부 중첩 객체

    @Getter
    @AllArgsConstructor
    public static class JobParams {
        private String VideoUrl;  // downloadUrl 값 (워크플로우의 Input 부분과 일치), 실제 분석할 파일의 다운로드 URL
    }
}
