package com.example.musica_be.service;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.review.ReviewRequestDto;
import com.example.musica_be.dto.review.ReviewResponseDto;
import com.example.musica_be.dto.review.UpdateReviewDto;
import com.example.musica_be.repository.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // 후기 등록
    public ReviewResponseDto createReview(User user, ReviewRequestDto dto) {
        Review review = Review.builder()
                .user(user)
                .lectureId(dto.getLectureId())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);

        return ReviewResponseDto.builder()
                .status("success")
                .message("리뷰가 등록되었습니다.")
                .reviewId(saved.getReviewId())
                .build();
    }

    // 후기 수정
    public ReviewResponseDto updateReview(User user, Integer reviewId, UpdateReviewDto dto) {
        Review review = reviewRepository.findByReviewIdAndUser(reviewId, user)
                .orElseThrow(() -> new NoSuchElementException("본인의 리뷰가 아닙니다."));

        review.update(dto.getComment(), dto.getRating());

        Review updated = reviewRepository.save(review);

        return ReviewResponseDto.builder()
                .status("success")
                .message("리뷰가 수정되었습니다.")
                .reviewId(updated.getReviewId())
                .build();
    }

    // 후기 삭제
    public ReviewResponseDto deleteReview(User user, Integer reviewId) {
        Review review = reviewRepository.findByReviewIdAndUser(reviewId, user)
                .orElseThrow(() -> new NoSuchElementException("본인의 리뷰가 아닙니다."));

        reviewRepository.delete(review);

        return ReviewResponseDto.builder()
                .status("success")
                .message("리뷰가 삭제되었습니다.")
                .reviewId(reviewId)
                .build();
    }
}
