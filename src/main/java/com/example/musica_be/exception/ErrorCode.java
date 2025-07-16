package com.example.musica_be.exception;

public enum ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND"),
    INVALID_PASSWORD("INVALID_PASSWORD"),
    INSTRUCTOR_NOT_APPROVED("INSTRUCTOR_NOT_APPROVED");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
