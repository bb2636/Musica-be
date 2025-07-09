package com.example.musica_be.service.mapper;

import com.example.musica_be.domain.answer.Answer;
import com.example.musica_be.domain.question.Question;
import com.example.musica_be.dto.answer.InstructorAnswerDto;
import com.example.musica_be.dto.question.QuestionDto;
import org.springframework.stereotype.Component;

@Component
public class QnaMapper {

    public QuestionDto toQuestionDto(Question question) {
        return QuestionDto.builder()
                .questionId(question.getId())
                .classId(question.getLecture().getId())
                .userId(question.getUser().getId())
                .question(question.getQuestion())
                .createdAt(question.getCreatedAt())
                .build();
    }

    public InstructorAnswerDto toInstructorAnswerDto(Answer answer) {
        return InstructorAnswerDto.builder()
                .question(answer.getQuestion().getQuestion())
                .title(answer.getQuestion().getLecture().getTitle())
                .createdAt(answer.getCreatedAt())
                .build();
    }
}