package com.example.musica_be.dto.answer;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorAnswerDto {
    private String question;
    private String title;
    private LocalDateTime createdAt;
}
