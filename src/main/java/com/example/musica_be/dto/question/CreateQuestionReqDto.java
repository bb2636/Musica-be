package com.example.musica_be.dto.question;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//질문 등록 요청
public class CreateQuestionReqDto {
    private Long userId;
    private Long classId;
    private String question;
}