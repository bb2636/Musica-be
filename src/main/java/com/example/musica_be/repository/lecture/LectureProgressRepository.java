package com.example.musica_be.repository.lecture;

import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureProgress;
import com.example.musica_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureProgressRepository extends JpaRepository<LectureProgress, Long> {
    Optional<LectureProgress> findByUserAndLecture(User user, Lecture lecture);
    List<LectureProgress> findByUserIdAndLecture_Classes_Id(Long userId, Long classId);
    @Query("SELECT COUNT(lp) FROM LectureProgress lp " +
        "WHERE lp.user.id = :userId AND lp.lecture.classes.id = :classId AND lp.isCompleted = true")
    int countByUserIdAndClassIdAndIsCompletedTrue(@Param("userId") Long userId, @Param("classId") Long classId);
    @Query("SELECT lp FROM LectureProgress lp WHERE lp.user.id = :userId AND lp.lecture.classes.id = :classId")
    List<LectureProgress> findAllByUserIdAndClassId(@Param("userId") Long userId, @Param("classId") Long classId);
    Optional<LectureProgress> findByUserIdAndLectureId(Long userId, Long lectureId);
    @Modifying
    @Query("DELETE FROM LectureProgress lp WHERE lp.lecture.id IN :lectureIds")
    void deleteByLectureIds(@Param("lectureIds") List<Long> lectureIds);
    List<LectureProgress> findAllByUserIdAndLectureId(Long userId, Long lectureId);
}
