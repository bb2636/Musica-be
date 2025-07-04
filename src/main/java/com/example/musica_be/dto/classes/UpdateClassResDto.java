package com.example.musica_be.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateClassResDto {
    private Long classId;
    private String message; // "수정 완료" 등
}
