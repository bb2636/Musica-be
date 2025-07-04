package com.example.musica_be.domain.classes;

import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "classes") // 테이블명도 classes로
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Classes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // 대용량 텍스트
    @Column(name = "description_html")
    private String descriptionHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    // 난이도는 user 도메인의 Level 엔티티 사용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "difficulty_id", nullable = false)
    private Level difficulty;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Column(name = "class_price", nullable = false)
    private Integer classPrice;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
