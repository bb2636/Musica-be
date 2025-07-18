package com.example.musica_be.service;

import com.example.musica_be.domain.answer.Answer;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.question.Question;
import com.example.musica_be.domain.question.QuestionStatus;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.answer.CreateAnswerReqDto;
import com.example.musica_be.dto.answer.CreateAnswerResDto;
import com.example.musica_be.dto.answer.InstructorAnswerDto;
import com.example.musica_be.dto.question.CreateQuestionReqDto;
import com.example.musica_be.dto.question.CreateQuestionResDto;
import com.example.musica_be.dto.question.QuestionDto;
import com.example.musica_be.dto.question.UpdateQuestionReqDto;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.qna.AnswerRepository;
import com.example.musica_be.repository.qna.QuestionRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.service.mapper.QnaMapper;
import com.example.musica_be.util.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QnaService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ClassesRepository classesRepository;
    private final LectureRepository lectureRepository;
    private final QnaMapper qnaMapper;

    // 질문 등록
    public CreateQuestionResDto createQuestion(CreateQuestionReqDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Classes classes = classesRepository.findById(request.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("클래스 없음"));

        List<Lecture> lectures = lectureRepository.findByClasses(classes);
        if (lectures.isEmpty()) {
            throw new IllegalArgumentException("해당 클래스에 등록된 강의 없음");
        }

        Lecture lecture = lectures.get(0);

        Question question = Question.builder()
                .user(user)
                .lecture(lecture)
                .question(request.getQuestion())
                .status(QuestionStatus.IN_PROGRESS)
                .build();

        Question saved = questionRepository.save(question);

        return CreateQuestionResDto.builder()
                .success(true)
                .questionId(saved.getId())
                .build();
    }

    // 질문 수정
    public void updateQuestion(Long questionId, UpdateQuestionReqDto request, Long userId) throws AccessDeniedException {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문 없음"));
        if (!question.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인만 수정 가능");
        }
        question.setQuestion(request.getQuestion());
    }

    // 질문 삭제
    public void deleteQuestion(Long questionId, Long userId) throws AccessDeniedException {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문 없음"));
        if (!question.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인만 삭제 가능");
        }
        questionRepository.delete(question);
    }

    // 강의별 질문 조회
    public List<QuestionDto> getQuestionsByClass(Long classId) {
        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("클래스 없음"));

        List<Lecture> lectures = lectureRepository.findByClasses(classes);
        if (lectures.isEmpty()) {
            return List.of();
        }
        // 여러 강의의 질문을 모두 모아서 반환
        return lectures.stream()
                .flatMap(lecture -> questionRepository.findByLectureId(lecture.getId()).stream())
                .map(qnaMapper::toQuestionDto)
                .toList();
    }

    // 답변 등록 (강사만)
    public CreateAnswerResDto createAnswer(CreateAnswerReqDto request) throws AccessDeniedException {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        if (!"INSTRUCTOR".equals(user.getRole())) {
            throw new AccessDeniedException("강사만 답변 가능");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("질문 없음"));

        Answer answer = new Answer();
        answer.setUser(user);
        answer.setQuestion(question);
        answer.setAnswer(request.getAnswer());

        Answer saved = answerRepository.save(answer);

        question.setStatus(QuestionStatus.ANSWERED);

        return CreateAnswerResDto.builder()
                .success(true)
                .answerId(saved.getId())
                .build();
    }

    // 강사 마이페이지 답변 조회
    public List<InstructorAnswerDto> getInstructorAnswers(Long instructorId) {
        return answerRepository.findByUserId(instructorId)
                .stream()
                .map(qnaMapper::toInstructorAnswerDto)
                .toList();
    }

    //유저 마이페이지 질문 조회
  public List<QuestionDto> getUserAnswers(String jwt){
    Long userIdFromToken = Long.valueOf(JwtUtils.getUserIdFromToken(jwt));
    return  questionRepository.findByUserId(userIdFromToken)
        .stream()
        .map(qnaMapper::toQuestionDto)
        .toList();
  }
}
