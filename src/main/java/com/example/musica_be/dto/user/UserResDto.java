package com.example.musica_be.dto.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResDto {
    private Long id;
    private String name;
    private String email;
    private String role; // "USER" or "INSTRUCTOR" or "ADMIN"
    private String level; // 레벨 이름 (Beginner, Intermediate, Advanced)
}
