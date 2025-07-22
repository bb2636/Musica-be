package com.example.musica_be.service;

import com.example.musica_be.dto.upload.PresignedUrlRequestDto;
import com.example.musica_be.dto.upload.PresignedUrlResponseDto;
import com.example.musica_be.util.S3PresignedUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.url-prefix}") // ex: https://musica-test-bk.s3.ap-northeast-2.amazonaws.com
    private String s3UrlPrefix;

    @Transactional
    public PresignedUrlResponseDto getPresignedUrl(PresignedUrlRequestDto request) {
        String originalFileName = request.getFileName();
        String extension = "";

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }

        String directory = extension.equalsIgnoreCase(".mp4") ? "lectures/" : "thumbnails/";
        String key = directory + UUID.randomUUID() + extension;

        String uploadUrl = S3PresignedUrl.generateUploadUrl(
            s3Presigner, bucket, key, request.getContentType(), Duration.ofMinutes(5)
        );
        System.out.println("uploadUrl = " + uploadUrl);

        String viewUrl = S3PresignedUrl.generateDownloadUrl(
            s3Presigner, bucket, key, Duration.ofMinutes(5)
        );

        String fileUrl = s3UrlPrefix + "/" + key;

        return PresignedUrlResponseDto.builder()
            .uploadUrl(uploadUrl)
            .fileUrl(fileUrl)
            .viewUrl(viewUrl)
            .objectKey(key) // 🔥 여기에 포함
            .build();
    }

    /**
     * S3 객체 URL 에서 객체 키(object key)를 추출하는 메서드
     *
     * 예: https://bucket.s3.amazonaws.com/lectures/abc.mp4?... → lectures/abc.mp4 추출
     *
     * @param url S3 Presigned upload URL
     * @return S3 내부 객체 경로 (object key)
     * @throws IllegalArgumentException 잘못된 URL 형식이거나 null/빈 문자열일 경우
     */
    private String extractVideoObjectKey(String url) {
        if (url == null || url.isBlank()) {
            return null; // 또는 예외 발생: URL 미제공
        }
        try {
            URI uri = new URI(url);
            return uri.getPath().substring(1); // "/lectures/abc.mp4" → "lectures/abc.mp4"
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 URL 형식입니다: " + url);
        }
    }
}
