package com.example.musica_be.exception;

import lombok.Getter;

@Getter
public class CustomAuthException extends RuntimeException {
    private final ErrorCode code;

    public CustomAuthException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

}
