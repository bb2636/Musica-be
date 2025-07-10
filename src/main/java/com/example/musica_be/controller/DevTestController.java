package com.example.musica_be.controller;

import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.question.Question;
import com.example.musica_be.domain.question.QuestionStatus;
import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.answer.CreateAnswerReqDto;
import com.example.musica_be.dto.answer.CreateAnswerResDto;
import com.example.musica_be.dto.question.CreateQuestionReqDto;
import com.example.musica_be.dto.question.CreateQuestionResDto;
import com.example.musica_be.repository.classes.CategoryRepository;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.qna.AnswerRepository;
import com.example.musica_be.repository.qna.QuestionRepository;
import com.example.musica_be.repository.user.LevelRepository;
import com.example.musica_be.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.musica_be.domain.classes.Category;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevTestController {

    private final UserRepository userRepository;
    private final ClassesRepository classesRepository;
    private final LectureRepository lectureRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final LevelRepository levelRepository;
    private final CategoryRepository categoryRepository;

    @PostMapping("/init-levels")
    public ResponseEntity<String> initLevels() {
        if (levelRepository.count() == 0) {
            levelRepository.save(new Level(null, "Beginner"));
            levelRepository.save(new Level(null, "Intermediate"));
            levelRepository.save(new Level(null, "Advanced"));
        }
        return ResponseEntity.ok("레벨 초기화 완료");
    }
    @PostMapping("/init-categories")
    public ResponseEntity<String> initCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(null, "PIANO", "피아노", 1, true));
            categoryRepository.save(new Category(null, "GUITAR", "기타", 2, true));
            categoryRepository.save(new Category(null, "DRUM", "드럼", 3, true));
            categoryRepository.save(new Category(null, "VIOLIN", "바이올린", 4, true));
            // 필요 시 더 추가 가능
        }
        return ResponseEntity.ok("카테고리 초기화 완료");
    }

    @PostMapping("/init")
    @Transactional
    public ResponseEntity<String> initTestData() {
        // 테스트용 레벨 가져오기 (id: 1 = Beginner)
        Level beginner = levelRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Beginner 레벨이 없습니다. 먼저 /init-levels 실행하세요."));
        // 테스트용 카테고리 가져오기
        Category pianoCategory = categoryRepository.findByCode("PIANO")
                .orElseThrow(() -> new IllegalArgumentException("PIANO 카테고리가 없습니다. 먼저 /init-categories 실행하세요."));

        // 사용자 생성
        User user = User.builder()
                .email("test2@test.com")
                .name("test")
                .password("test")
                .role(Role.USER)
                .isApproved(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        User instructor = User.builder()
                .email("inst@test.com")
                .name("강사")
                .password("test")
                .role(Role.INSTRUCTOR)
                .isApproved(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(instructor);

        // 클래스 등록
        Classes classes = Classes.builder()
                .title("테스트 클래스")
                .descriptionHtml("클래스 설명")
                .category(pianoCategory)
                .difficulty(beginner) // 필요시 level 객체 추가
                .instructor(instructor)
                .classPrice(10000)
                .build();
        classesRepository.save(classes);

        // 강의 등록
        Lecture lecture = Lecture.builder()
                .title("테스트 강의")
                .videoUrl("https://youtube.com/video")
                .fileObjectKey("lectures/sample/dummy.mp4")
                .fileUrl("https://sheet.com/music.pdf")
                .lectureOrder(1)
                .classes(classes)
                .build();
        lectureRepository.save(lecture);

        return ResponseEntity.ok("테스트 데이터 초기화 완료");
    }

    @PostMapping("/question")
    public ResponseEntity<CreateQuestionResDto> testCreateQuestion(@RequestBody CreateQuestionReqDto req) {
        return ResponseEntity.ok(
                CreateQuestionResDto.builder()
                        .success(true)
                        .questionId(1L)
                        .build()
        );
    }

    @PostMapping("/answer")
    public ResponseEntity<CreateAnswerResDto> testCreateAnswer(@RequestBody CreateAnswerReqDto req) {
        return ResponseEntity.ok(
                CreateAnswerResDto.builder()
                        .success(true)
                        .answerId(1L)
                        .build()
        );
    }
}
