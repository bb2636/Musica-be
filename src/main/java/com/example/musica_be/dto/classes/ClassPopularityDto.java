package com.example.musica_be.dto.classes;

import com.example.musica_be.domain.classes.Classes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClassPopularityDto {
    // 내부용(점수 계산 및 정렬)
    private Classes classes;
    private int score;
}
