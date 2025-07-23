package com.example.musica_be.repository.qna;

import com.example.musica_be.domain.answer.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByQuestionId(Long questionId);
    List<Answer> findByUserId(Long userId);

    // ✅ 강의 목록에 속한 질문들의 답변 먼저 삭제
    @Modifying
    @Query("DELETE FROM Answer a WHERE a.question.lecture.id IN :lectureIds")
    void deleteByLectureIds(@Param("lectureIds") List<Long> lectureIds);
}