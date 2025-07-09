package com.example.musica_be.repository.qna;

import com.example.musica_be.domain.question.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByLectureId(Long lectureId);
    List<Question> findByUserId(Long userId);
}