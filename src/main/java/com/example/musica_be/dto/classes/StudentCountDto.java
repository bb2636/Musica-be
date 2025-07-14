package com.example.musica_be.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentCountDto {
    private Long classId;
    private Long count;
}
