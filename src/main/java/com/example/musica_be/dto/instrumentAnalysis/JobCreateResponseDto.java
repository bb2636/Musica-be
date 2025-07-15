package com.example.musica_be.dto.instrumentAnalysis;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class JobCreateResponseDto {
    // JSON 의 "id"에 정확히 매핑됨
    // 응답 예시: {"id":"a913v240-7ce1-3571-a22q-f4q3cdabcd1e"}
    private String id;
}
