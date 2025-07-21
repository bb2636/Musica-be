package com.example.musica_be.dto.upload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PresignedUrlResponseDto {
    private String uploadUrl;
    private String fileUrl;
    private String viewUrl;
    private String objectKey; // 🔥 추가
}