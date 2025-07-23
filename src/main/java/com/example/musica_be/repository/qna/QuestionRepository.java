package com.example.musica_be.repository.qna;

import com.example.musica_be.domain.question.Question;
import com.example.musica_be.domain.question.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByLectureId(Long lectureId);

    List<Question> findByUserId(Long userId);

    // 강사 미답변 질문 수
    @Query("""
            SELECT COUNT(q)
            FROM Question q
            WHERE q.lecture.classes.instructor.id = :instructorId
              AND q.status = com.example.musica_be.domain.question.QuestionStatus.IN_PROGRESS
        """)
    int countPendingByInstructorId(@Param("instructorId") Long instructorId);

    List<Question> findTop3ByLecture_Classes_Instructor_IdOrderByCreatedAtDesc(Long instructorId);

    List<Question> findByLectureIdAndStatus(Long lectureId, QuestionStatus status);

    @Modifying
    @Query("DELETE FROM Question q WHERE q.lecture.id IN :lectureIds")
    void deleteByLectureIds(@Param("lectureIds") List<Long> lectureIds);
}