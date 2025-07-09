package com.example.musica_be.dto.answer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAnswerResDto {
    private boolean success;
    private Long answerId;
}
