package com.example.musica_be.dto.instrumentAnalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponseDto {
    private String uploadUrl;   // 업로드용 URL
    private String downloadUrl; // 다운로드용 URL
}
