package com.example.musica_be.dto.upload;

import lombok.Getter;

@Getter
public class PresignedUrlRequestDto {
    private String fileName;
    private String contentType;
}
