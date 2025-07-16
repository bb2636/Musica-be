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
    private String message;  // 예외 메시지 또는 추가적인 응답 메시지
    private boolean isApproved;

    // User 객체를 받아서 DTO를 변환하는 생성자
    public UserResDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().name();  // Role Enum을 String으로 변환
        this.level = user.getLevel() != null ? user.getLevel().getName() : null;  // Level의 이름 (예: Beginner, Intermediate, Advanced)
        this.isApproved = user.isApproved();
    }

    // 추가된 생성자: 예외 메시지를 포함한 생성자
    public UserResDto(String message) {
        this.message = message;  // 예외 메시지나 성공 메시지 설정
    }
}
