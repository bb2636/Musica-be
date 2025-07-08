package com.example.musica_be.repository.review;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByReviewIdAndUser(Integer reviewId, User user); // 본인 확인용

    List<Review> findByUserId(Long userId);
}
