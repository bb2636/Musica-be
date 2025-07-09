package com.example.musica_be.service.review;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.review.ReviewRequestDto;
import com.example.musica_be.dto.review.ReviewResponseDto;
import com.example.musica_be.dto.review.UpdateReviewDto;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;

    // 후기 등록
    @Transactional
    public ReviewResponseDto createReview(User user, ReviewRequestDto dto) {
        // 강의 조회(존재 유무 확인)
        Lecture lecture = lectureRepository.findById(dto.getLectureId())
                .orElseThrow(() -> new NoSuchElementException("해당 강의를 찾을 수 없습니다."));

        // 리뷰 객체 생성
        Review review = Review.builder()
                .user(user)
                .lecture(lecture)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        // DB 에 저장
        Review saved = reviewRepository.save(review);

        // 응답 객체로 변환하여 반환
        return ReviewResponseDto.builder()
                .status("success")
                .message("리뷰가 등록되었습니다.")
                .reviewId(saved.getReviewId())
                .build();
    }

    // 후기 수정
    @Transactional
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
    @Transactional
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

    // 1. 내 후기 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByUser(User user) {
        List<Review> reviews = reviewRepository.findAllByUser(user);
        return reviews.stream()
                .map(r -> toDto(r, user.getId()))
                .collect(Collectors.toList());
    }

    // 2. 클래스별 후기 목록
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByClass(Long classId, Long currentUserId) {
        return reviewRepository.findAllByClassesIdWithUser(classId).stream()
                .map(r -> toDto(r, currentUserId))
                .collect(Collectors.toList());
    }

    // 3. 강의별 후기 목록
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByLecture(Long lectureId, Long currentUserId) {
        List<Review> reviews = reviewRepository.findAllByLectureIdWithUser(lectureId);
        return reviews.stream()
                .map(r -> toDto(r, currentUserId))
                .collect(Collectors.toList());
    }

    // 4. 후기 단건 조회
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Integer reviewId, Long currentUserId) {
        Review review = reviewRepository.findByReviewIdWithUser(reviewId)
                .orElseThrow(() -> new NoSuchElementException("리뷰를 찾을 수 없습니다."));
        return toDto(review, currentUserId);
    }


    // DTO 변환 공통 로직
    private ReviewResponseDto toDto(Review r, Long currentUserId) {
        return ReviewResponseDto.builder()
                .reviewId(r.getReviewId())
                .name(r.getUser().getName())
                .rating(r.getRating())
                .comment(r.getComment())
                .progress(getProgress(r.getUser().getId(), r.getLecture().getId()))
                .isAuthor(currentUserId != null && r.getUser().getId().equals(currentUserId))
                .createdAt(r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .lectureId(r.getLecture().getId())
                .status("success")
                .message("조회 성공")
                .build();
    }

    // 시청률 (임시로 0 리턴)
    private int getProgress(Long userId, Long lectureId) {
        // TODO: UserLog 테이블에서 실제 시청률을 조회하도록 변경
        return 0;
    }
}
