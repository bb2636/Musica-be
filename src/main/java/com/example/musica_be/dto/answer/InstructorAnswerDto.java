package com.example.musica_be.dto.answer;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorAnswerDto {
    private Long questionId;
    private String question;
    private String title;
    private String answer;
    private LocalDateTime createdAt;
}
