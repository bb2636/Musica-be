package com.example.musica_be.dto.instructor;

import com.example.musica_be.domain.user.ApprovalStatus;
import com.example.musica_be.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InstructorInfoDto {

    private Long id;
    private String name;
    private String email;
    private boolean isApproved;            // 승인 여부 (관리자 승인 여부 포함)
    private ApprovalStatus approvalStatus; // 대기/승인/거절 상태
    private String levelName;              // 레벨 이름 (nullable 가능)
    private LocalDateTime createdAt;

    public static InstructorInfoDto fromEntity(User user) {
        return InstructorInfoDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .isApproved(user.isApproved())
            .approvalStatus(user.getApprovalStatus())
            .levelName(user.getLevel() != null ? user.getLevel().getName() : null)
            .createdAt(user.getCreatedAt())
            .build();
    }
}