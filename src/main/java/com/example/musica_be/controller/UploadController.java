// controller/UploadController.java
package com.example.musica_be.controller;

import com.example.musica_be.dto.upload.PresignedUrlRequestDto;
import com.example.musica_be.dto.upload.PresignedUrlResponseDto;
import com.example.musica_be.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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