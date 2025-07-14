package com.example.musica_be.dto.user;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}