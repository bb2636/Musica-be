package com.example.musica_be.repository.review;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user u WHERE r.lectureId = :lectureId ORDER BY r.createdAt DESC")
    List<Review> findAllByLectureIdWithUser(@Param("lectureId") Integer lectureId);

    @Query("SELECT r FROM Review r JOIN FETCH r.user u WHERE r.reviewId = :reviewId")
    Optional<Review> findByReviewIdWithUser(@Param("reviewId") Integer reviewId);

    List<Review> findAllByUser(User user); // 마이페이지 조회용

    Optional<Review> findByReviewIdAndUser(Integer reviewId, User user); // 본인 확인용
}
