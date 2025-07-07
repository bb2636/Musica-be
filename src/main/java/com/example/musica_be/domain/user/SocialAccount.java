package com.example.musica_be.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String socialId;  // 카카오 ID, 구글 ID 등

    @Column(nullable = false)
    private String provider;  // 소셜 로그인 제공자 (예: "kakao", "google")

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 연결된 사용자 (User 엔티티와 관계 설정)

}
