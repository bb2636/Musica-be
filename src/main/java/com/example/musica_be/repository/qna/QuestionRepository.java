package com.example.musica_be.repository.qna;

import com.example.musica_be.domain.question.Question;
import com.example.musica_be.domain.question.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByLectureId(Long lectureId);

    List<Question> findByUserId(Long userId);

    @Query("""
        SELECT COUNT(q)
        FROM Question q
        WHERE q.lecture.classes.instructor.id = :instructorId
        AND q.status = 'IN_PROGRESS'
        """)
    int countPendingByInstructorId(@Param("instructorId") Long instructorId);

    List<Question> findTop3ByLecture_Classes_Instructor_IdOrderByCreatedAtDesc(Long instructorId);

    List<Question> findByLectureIdAndStatus(Long lectureId, QuestionStatus status);
}