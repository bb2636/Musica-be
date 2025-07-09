package com.example.musica_be.dto.question;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//질문 등록 응답
public class CreateQuestionResDto {
    private boolean success;
    private Long questionId;
}
