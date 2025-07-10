package com.example.musica_be.dto.category;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryReqDto {
    private String code; // 카테고리 고유 코드 (예: PIANO, GUITAR)
    private String displayName; // 사용자에게 보여질 이름 (예: 피아노, 기타)
    private int displayOrder; // 정렬 순서
    private boolean isActive; // 노출 여부 (true: 노출, false: 숨김)
}
