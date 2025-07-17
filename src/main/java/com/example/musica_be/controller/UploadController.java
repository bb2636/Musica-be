// controller/UploadController.java
package com.example.musica_be.controller;

import com.example.musica_be.dto.upload.PresignedUrlRequestDto;
import com.example.musica_be.dto.upload.PresignedUrlResponseDto;
import com.example.musica_be.service.UploadService;
import com.example.musica_be.util.S3PresignedUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * 프론트가 fileName, contentType 보내면 presigned URL(uploadUrl, DownloadUrl) 생성해서 반환
     */
    @PostMapping("/presigned")
    public ResponseEntity<PresignedUrlResponseDto> getPresignedUrl(@RequestBody PresignedUrlRequestDto request) {
        return ResponseEntity.ok(uploadService.getPresignedUrl(request));
    }

}