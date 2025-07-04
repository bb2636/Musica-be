package com.example.musica_be.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteClassResDto {
    private Long classId;
    private String message;  // 예: "클래스가 삭제되었습니다."
}