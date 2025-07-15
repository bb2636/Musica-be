package com.example.musica_be.dto.instrumentAnalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonProperty;

// 외부 api (music.ai api)와의 통신에 사용되는 dto
@Getter
@AllArgsConstructor
public class MusicAiJobRequestDto {
    private String name;
    private String workflow; // 실제로는 slug 값이 들어감
    private Params params;

    @Getter
    @AllArgsConstructor
    public static class Params {
        @JsonProperty("VideoUrl")
        private String videoUrl;
    }
}