package com.example.musica_be.service.instructor;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.question.Question;
import com.example.musica_be.domain.user.Role;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.instructor.*;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.qna.QuestionRepository;
import com.example.musica_be.repository.review.ReviewRepository;
import com.example.musica_be.repository.user.UserRepository;
import com.example.musica_be.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {
    private final ClassesRepository classesRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final PasswordEncoder passwordEncoder;

    // 강사 마이페이지 대시보드에 보일 내용들
    @Transactional
    public InstructorDashboardResDto getInstructorDashboard(Long instructorId) {

        // 1. 강사 정보
        User instructor = userRepository.findById(instructorId)
            .orElseThrow(() -> new RuntimeException("강사 없음"));

        int totalClasses = classesRepository.countByInstructorId(instructorId);
        int totalStudents = classesRepository.countDistinctStudentsByInstructorId(instructorId);

        // 2. 통계 정보
        int totalRevenue = paymentItemRepository.sumTotalRevenueByInstructorId(instructorId);
        int monthlyRevenue = paymentItemRepository.sumMonthlyRevenueByInstructorId(instructorId);
        int pendingQuestions = questionRepository.countPendingByInstructorId(instructorId);
        int totalReviews = reviewRepository.countByInstructorId(instructorId);
        double averageRating = reviewRepository.averageRatingByInstructorId(instructorId);

        // 3. 최근 활동
        List<InstructorDashboardResDto.Activity> activities = new ArrayList<>();

        List<Question> recentQuestions = questionRepository
            .findTop3ByLecture_Classes_Instructor_IdOrderByCreatedAtDesc(instructorId);

        List<Review> recentReviews = reviewRepository
            .findTop3ByClasses_Instructor_IdOrderByCreatedAtDesc(instructorId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Question q : recentQuestions) {
            activities.add(InstructorDashboardResDto.Activity.builder()
                .type("question")
                .message("질문: " + q.getQuestion())
                .timestamp(q.getCreatedAt().format(formatter))
                .build());
        }

        for (Review r : recentReviews) {
            activities.add(InstructorDashboardResDto.Activity.builder()
                .type("review")
                .message("리뷰: " + r.getComment())
                .timestamp(r.getCreatedAt().format(formatter))
                .build());
        }

        return InstructorDashboardResDto.builder()
            .instructorInfo(InstructorDashboardResDto.InstructorInfo.builder()
                .name(instructor.getName())
                .email(instructor.getEmail())
                .totalClasses(totalClasses)
                .totalStudents(totalStudents)
                .build())
            .stats(InstructorDashboardResDto.Statistics.builder()
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .pendingQuestions(pendingQuestions)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .build())
            .recentActivities(activities)
            .build();
    }

    // 특정 강사의 후기 모아보기
    @Transactional
    public PagedResponse<InstructorReviewDto> getReviewsByInstructor(
        String jwt, int page, int size, Long classId, String sort) {

        Long userId = JwtUtils.extractUserId(jwt);
        User instructor = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("강사 정보를 찾을 수 없습니다."));

        List<Classes> classesList = classesRepository.findByInstructor(instructor);

        // 클래스 ID 필터가 있을 경우 해당 클래스만 선택
        List<Classes> targetClasses = (classId != null)
            ? classesList.stream().filter(c -> c.getId().equals(classId)).toList()
            : classesList;

        if (targetClasses.isEmpty()) {
            return new PagedResponse<>(List.of(), 0, 0, page, size);
        }

        // 정렬 처리
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));

        Page<Review> reviewPage = reviewRepository.findByClassesIn(targetClasses, pageable);

        List<InstructorReviewDto> dtoList = reviewPage.getContent().stream()
            .map(review -> InstructorReviewDto.builder()
                .reviewId(review.getReviewId())
                .reviewerName(review.getUser().getName())
                .comment(review.getComment())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt().toString())
                .classId(review.getClasses().getId())
                .classTitle(review.getClasses().getTitle())
                .lectureId(review.getLecture().getId())
                .lectureTitle(review.getLecture().getTitle())
                .build())
            .toList();

        return PagedResponse.<InstructorReviewDto>builder()
            .content(dtoList)
            .totalElements(reviewPage.getTotalElements())
            .totalPages(reviewPage.getTotalPages())
            .pageNumber(reviewPage.getNumber())
            .pageSize(reviewPage.getSize())
            .build();
    }

    // 특정 강사의 개인 정보 반환
    @Transactional
    public InstructorInfoDto getInstructorInfo(String jwt) {
        // 강사의 아이디 추출
        Long instructorId = JwtUtils.extractUserId(jwt);
        // 해당 강사의 아이디로 유저의 정보를 DTO에 매핑하여 받음
        User instructor = userRepository.findById(instructorId)
            .orElseThrow(() -> new RuntimeException("강사 없음"));

        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new AccessDeniedException("강사 권한이 필요합니다.");
        }

        return InstructorInfoDto.fromEntity(instructor);
    }

    // InstructorService.java
    @Transactional
    public InstructorInfoDto updateInstructorInfo(String jwt, InstructorUpdateRequestDto dto) {
        Long userId = JwtUtils.extractUserId(jwt);
        User instructor = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("강사를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), instructor.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        instructor.setName(dto.getName());
        instructor.setEmail(dto.getEmail());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            instructor.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        return InstructorInfoDto.fromEntity(instructor); // ✅ 수정된 사용자 정보 반환
    }

    // 특정 강사의 클래스

    // 특정 강사의 정산 내역


}
