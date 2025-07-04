package com.example.musica_be.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterReqDto {
    private String name;
    private String email;
    private String password;
    private String role; // "USER" or "INSTRUCTOR"
    private Long levelId; // 사용자의 레벨 ID
}
