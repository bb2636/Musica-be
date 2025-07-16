package com.example.musica_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomAuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(CustomAuthException e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());
        response.put("code", e.getCode().getCode());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}