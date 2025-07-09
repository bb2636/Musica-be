package com.example.musica_be.domain.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카테고리 코드 (예: PIANO, GUITAR)
    @Column(nullable = false, unique = true)
    private String code;

    // 사용자에게 보여질 이름 (예: 피아노, 기타)
    @Column(nullable = false)
    private String displayName;

    // 정렬 순서
    @Column(nullable = false)
    private int displayOrder;

    // 노출 여부 (관리자가 숨기거나 할 때 사용)
    @Column(nullable = false)
    private boolean isActive;
}