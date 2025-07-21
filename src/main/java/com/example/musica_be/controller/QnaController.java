package com.example.musica_be.controller;

import com.example.musica_be.dto.answer.CreateAnswerReqDto;
import com.example.musica_be.dto.answer.CreateAnswerResDto;
import com.example.musica_be.dto.answer.InstructorAnswerDto;
import com.example.musica_be.dto.question.CreateQuestionReqDto;
import com.example.musica_be.dto.question.CreateQuestionResDto;
import com.example.musica_be.dto.question.QuestionDto;
import com.example.musica_be.dto.question.UpdateQuestionReqDto;
import com.example.musica_be.service.QnaService;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QnaController {
    private final QnaService qnaService;

    // 질문 등록
    @PostMapping("/users/questions")
    public ResponseEntity<CreateQuestionResDto> createQuestion(
        @RequestHeader("Authorization") String jwt,
        @RequestBody CreateQuestionReqDto request) {
        Long userId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(qnaService.createQuestion(request, userId));
    }

    // 질문 수정
    @PutMapping("/users/questions/{questionId}")
    public ResponseEntity<Void> updateQuestion(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long questionId,
        @RequestBody UpdateQuestionReqDto request) throws Exception {
        Long userId = JwtUtils.extractUserId(jwt);
        qnaService.updateQuestion(questionId, request, userId);
        return ResponseEntity.ok().build();
    }

    // 질문 삭제
    @DeleteMapping("/users/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long questionId) throws Exception {
        Long userId = JwtUtils.extractUserId(jwt);
        qnaService.deleteQuestion(questionId, userId);
        return ResponseEntity.ok().build();
    }

    // 특정 클래스에 등록된 모든 질문 조회
    @GetMapping("/classes/{classId}/questions")
    public ResponseEntity<List<QuestionDto>> getQuestionsByClass(
        @PathVariable Long classId) {
        return ResponseEntity.ok(qnaService.getQuestionsByClass(classId));
    }

    // 답변 등록 (강사만)
    @PutMapping("/instructors/answers")
    public ResponseEntity<CreateAnswerResDto> createAnswer(
        @RequestHeader("Authorization") String jwt,
        @RequestBody CreateAnswerReqDto request) throws Exception {
        Long userId = JwtUtils.extractUserId(jwt);
        request.setUserId(userId);
        return ResponseEntity.ok(qnaService.createAnswer(request));
    }

    // 강사 마이페이지에서 자신의 답변 리스트 조회
    @GetMapping("/instructors/answers")
    public ResponseEntity<List<InstructorAnswerDto>> getInstructorAnswers(
        @RequestHeader("Authorization") String jwt) {
        Long userId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(qnaService.getInstructorAnswers(userId));
    }

    // 강사의 질문 리스트 조회
    @GetMapping("/instructors/questions")
    public ResponseEntity<List<QuestionDto>> getQuestionsForInstructor(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) String status // PENDING or ANSWERED
    ) {
        Long instructorId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(qnaService.getQuestionsForInstructor(instructorId, status));
    }

}