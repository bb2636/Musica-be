package com.example.musica_be.dto.user;

import com.example.musica_be.domain.user.User;
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

    // User 객체를 받아서 DTO를 변환하는 생성자
    public UserResDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().name();  // Role Enum을 String으로 변환
        this.level = user.getLevel().getName();  // Level의 이름 (예: Beginner, Intermediate, Advanced)
    }
}
