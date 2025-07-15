package com.example.musica_be.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClassesStudentCountDto {
    private Long classId;
    private Long studentCount;
}
