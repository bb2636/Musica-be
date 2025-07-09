package com.example.musica_be.dto.instrumentAnalysis;

import lombok.Getter;

@Getter
public class UrlResponseDto {
    private String uploadUrl;   // 업로드용 URL
    private String downloadUrl; // 다운로드용 URL
//    private String filePath;    // GCS 내부 경로
}
