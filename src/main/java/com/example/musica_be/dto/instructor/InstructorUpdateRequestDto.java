package com.example.musica_be.dto.instructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorUpdateRequestDto {
    private String name;
    private String email;
    private String currentPassword;
    private String newPassword;
}
