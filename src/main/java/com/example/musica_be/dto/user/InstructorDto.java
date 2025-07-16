package com.example.musica_be.dto.user;

import com.example.musica_be.domain.user.User;
import com.example.musica_be.domain.user.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 🎯 강사 정보 전송용 DTO
 * - 프론트엔드에서 필요한 강사 정보만 포함
 * - 민감한 정보(비밀번호 등) 제외
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorDto {

    private Long id;
    private String name;
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private ApprovalStatus approvalStatus; // ✅ enum 타입으로 변경

    // 🔄 User 엔티티를 DTO로 변환하는 생성자
    public InstructorDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.approvalStatus = user.getApprovalStatus() != null ?
                user.getApprovalStatus() : ApprovalStatus.PENDING; // ✅ enum 직접 할당
    }

    // 🔄 User 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static InstructorDto from(User user) {
        return InstructorDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .approvalStatus(user.getApprovalStatus() != null ?
                        user.getApprovalStatus() : ApprovalStatus.PENDING) // ✅ enum 직접 할당
                .build();
    }
}