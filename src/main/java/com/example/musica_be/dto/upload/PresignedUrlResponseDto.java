package com.example.musica_be.dto.upload;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedUrlResponseDto {
    private String uploadUrl; // S3에 업로드할 수 있는 PUT presigned URL
    private String fileUrl;   // S3 내부 경로 (DB 저장용)
    private String viewUrl;   // 사용자에게 보여줄 이미지 미리보기용 GET URL
}