package com.example.musica_be.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateClassResDto {
    private Long classId;
    private String message;  // ex) "클래스 등록 완료"
}
