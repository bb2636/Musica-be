package com.example.musica_be.repository.review;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.classes.ClassesRatingAvgDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    // 클래스별 후기 목록 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.user u WHERE r.lecture.classes.id = :classId ORDER BY r.createdAt DESC")
    List<Review> findAllByClassesIdWithUser(@Param("classId") Long classId);

    // 강의별 후기 목록 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.user u WHERE r.lecture.id = :lectureId ORDER BY r.createdAt DESC")
    List<Review> findAllByLectureIdWithUser(@Param("lectureId") Long lectureId);

    // 후기 단건 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.user u WHERE r.reviewId = :reviewId")
    Optional<Review> findByReviewIdWithUser(@Param("reviewId") Integer reviewId);

    List<Review> findAllByUser(User user); // 마이페이지 조회용

    Optional<Review> findByReviewIdAndUser(Integer reviewId, User user); // 본인 확인용

    List<Review> findByUserId(Long userId);

    @Query("""
    SELECT new com.example.musica_be.dto.classes.ClassesRatingAvgDto(r.classes.id, AVG(r.rating))
    FROM Review r
    GROUP BY r.classes.id
    """)
    List<ClassesRatingAvgDto> getAverageRatingsByClassIds(List<Long> classIds);

    // 클래스별 평균 별점 구하는 로직 - (FE) ClassCard 표시용
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.classes.id = :classId")
    Optional<Double> calculateAverageRatingByClassId(@Param("classId") Long classId);
}
