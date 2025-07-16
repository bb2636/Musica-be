package com.example.musica_be.service.instructor;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.question.Question;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.instructor.InstructorDashboardResDto;
import com.example.musica_be.repository.classes.ClassesRepository;
import com.example.musica_be.repository.payment.PaymentItemRepository;
import com.example.musica_be.repository.qna.QuestionRepository;
import com.example.musica_be.repository.review.ReviewRepository;
import com.example.musica_be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorDashboardService {
    private final ClassesRepository classesRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentItemRepository paymentItemRepository;

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
}
