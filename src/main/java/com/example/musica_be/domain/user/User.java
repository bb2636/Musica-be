package com.example.musica_be.domain.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "level_id", nullable = true)  // `role`이 USER일 경우만 필요
    private Level level;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 추가된 `isApproved` 필드의 setter와 getter 메서드
    @Column(nullable = false)
    private boolean isApproved;  // 승인 여부 필드 추가

    public void setIsApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }

    // `INSTRUCTOR`일 경우 승인 여부를 확인하는 메서드
    public boolean isInstructorApproved() {
        return this.role == Role.INSTRUCTOR && this.isApproved;
    }
}