package com.example.musica_be.dto.question;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//질문 수정 요청
public class UpdateQuestionReqDto {
    private String question;
}