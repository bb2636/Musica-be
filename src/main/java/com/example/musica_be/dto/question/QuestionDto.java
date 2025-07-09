package com.example.musica_be.dto.question;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//질문 단건 조회/리스트
public class QuestionDto {
    private Long questionId;
    private Long classId;
    private Long userId;
    private String question;
    private LocalDateTime createdAt;
}