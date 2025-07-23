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
        .question(question.getQuestion())
        .questionId(question.getId())
        .classId(question.getLecture().getClasses().getId()) // ✅ Classes ID
        .lectureId(question.getLecture().getId())             // ✅ Lecture ID
        .userId(question.getUser().getId())
        .answer(question.getAnswer().getAnswer())
        .createdAt(question.getCreatedAt())
        .build();
  }

  public InstructorAnswerDto toInstructorAnswerDto(Answer answer) {
    return InstructorAnswerDto.builder()
        .question(answer.getQuestion().getQuestion())
        .title(answer.getQuestion().getLecture().getTitle())
        .answer(answer.getAnswer())
        .questionId(answer.getQuestion().getId())
        .createdAt(answer.getCreatedAt())
        .build();
  }
}