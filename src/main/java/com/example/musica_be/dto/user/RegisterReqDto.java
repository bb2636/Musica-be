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
    private String role; // "USER" or "INSTRUCTOR" or "ADMIN"
    private Long levelId; // 사용자의 레벨 ID (role이 USER일 경우만 고를 수 있음)

    public boolean isRoleUser() {
        return "USER".equalsIgnoreCase(role);
    }
}
