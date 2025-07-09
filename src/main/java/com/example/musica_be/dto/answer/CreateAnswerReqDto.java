package com.example.musica_be.dto.answer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAnswerReqDto {
    private Long userId;     // 강사 ID
    private Long questionId;
    private String answer;
}