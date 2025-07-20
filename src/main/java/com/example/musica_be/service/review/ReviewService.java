package com.example.musica_be.service.review;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.user.Level;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.instructor.InstructorReviewDto;
import com.example.musica_be.dto.instructor.PagedResponse;
import com.example.musica_be.dto.review.ReviewRequestDto;
import com.example.musica_be.dto.review.ReviewResponseDto;
import com.example.musica_be.dto.review.ReviewSummaryCardDto;
import com.example.musica_be.dto.review.UpdateReviewDto;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.lecture.LectureRepository;
import com.example.musica_be.repository.review.ReviewRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ClassesRepository classRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    @Qualifier("openAiWebClient")
    private final WebClient openAiWebClient; // 주입받음

    // 후기 등록
    @Transactional
    public ReviewResponseDto createReview(User user, ReviewRequestDto dto) {
        Classes classes = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("해당 클래스가 없습니다."));
        // 강의 조회(존재 유무 확인)
        Lecture lecture = lectureRepository.findById(dto.getLectureId())
                .orElseThrow(() -> new NoSuchElementException("해당 강의가 없습니다."));

        // 리뷰 객체 생성
        Review review = Review.builder()
                .user(user)
                .lecture(lecture)
                .classes(classes)
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

    // 1. 마이페이지 후기 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByUser(User user) {
        List<Review> reviews = reviewRepository.findAllByUser(user);
        return reviews.stream()
                .map(r -> toDto(r, user.getId()))
                .collect(Collectors.toList());
    }

    // 2. 클래스별 후기 목록 조회
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

    // 리뷰 엔티티 -> 응답 DTO 변환 (공통 로직)
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

    // 시청률 계산 (향후 UserLog 기준으로 구현예정)
    private int getProgress(Long userId, Long lectureId) {
        // TODO: UserLog 테이블에서 실제 시청률을 조회하도록 변경
        return 0;
    }

    // GPT용: 강의 ID 기준 전체 리뷰 댓글 원문 연결
    public String getRawCommentsByLecture(Long lectureId) {
        List<Review> reviews = reviewRepository.findAllByLectureIdWithUser(lectureId);
        return reviews.stream()
                .map(Review::getComment)
                .collect(Collectors.joining("\n"));
    }

    // GPT용: 클래스 ID 기준 전체 리뷰 댓글 원문 연결
    private String getRawCommentsByClass(Long classId) {
        return reviewRepository.findAllByClassesIdWithUser(classId).stream()
                .map(Review::getComment)
                .collect(Collectors.joining("\n"));
    }

    // 유저 이름 마스킹 처리 (예: ej*******88)
    private String maskUsername(String name) {
        if (name.length() <= 2) return name.charAt(0) + "*";
        return name.substring(0, 2) + "*".repeat(name.length() - 2);
    }

    // OpenAI GPT를 통해 수강 후기 요약 요청
    public String summarizeWithOpenAI(String inputText) {

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "다음 수강후기를 간단하게 한줄 요약해줘."),
                        Map.of("role", "user", "content", inputText)
                ),
                "temperature", 0.7
        );

        return openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .exchangeToMono(response -> {
                    return response.bodyToMono(String.class)
//                            .doOnNext(raw -> System.out.println("📥 응답 본문: " + raw))
                            .map(body -> {
                                try {
                                    // JSON 직접 파싱
                                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    var map = mapper.readValue(body, Map.class);
                                    var choices = (List<Map<String, Object>>) map.get("choices");
                                    if (choices != null && !choices.isEmpty()) {
                                        var message = (Map<String, Object>) choices.get(0).get("message");
                                        return (String) message.get("content");
                                    }
                                    return "요약 결과 없음";
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return "요약 처리 중 오류 발생";
                                }
                            });
                })
                .onErrorReturn("OpenAI API 호출 실패")
                .block();
    }

    // 메인페이지용: 최신 5점 리뷰 6개를 요약 카드 형태로 반환
    @Transactional(readOnly = true)
    public List<ReviewSummaryCardDto> getReviewSummaryCards() {
        Pageable pageable = PageRequest.of(0, 6);
        List<Review> reviews = reviewRepository.findTop6ByRatingIsFiveOrderByCreatedAtDesc(pageable);

        return reviews.stream()
                .map(r -> {
                    String summary;
                    String maskedUsername;

                    // 1. GPT 요약 처리
                    try {
                        String comment = Optional.ofNullable(r.getComment()).orElse("");
                        summary = summarizeWithOpenAI(comment);
                    } catch (Exception e) {
                        summary = "[요약 실패: OpenAI 호출 오류]";
                        e.printStackTrace();
                    }

                    // 2. 유저 이름 마스킹 처리
                    try {
                        String username = Optional.ofNullable(r.getUser())
                                .map(User::getName)
                                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음"));
                        maskedUsername = maskUsername(username);
                    } catch (Exception e) {
                        maskedUsername = "[익명 사용자: 유저 정보 없음]";
                        e.printStackTrace();
                    }
                    // 3. DTO 변환 (null-safe → 내부에서 처리)
                    return ReviewSummaryCardDto.from(r, summary, maskedUsername);
                })
                .collect(Collectors.toList());
    }

}
