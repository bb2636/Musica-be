package com.example.musica_be.dto.instructor;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InstructorDashboardResDto {
    private InstructorInfo instructorInfo;
    private Statistics stats;
    private List<Activity> recentActivities;

    @Getter
    @Builder
    public static class InstructorInfo {
        private String name;
        private String email;
        private int totalClasses;
        private int totalStudents;
    }

    @Getter
    @Builder
    public static class Statistics {
        private int totalRevenue;
        private int monthlyRevenue;
        private int pendingQuestions;
        private int totalReviews;
        private double averageRating;
    }

    @Getter
    @Builder
    public static class Activity {
        private String type; // "question", "review" 등
        private String message;
        private String timestamp;
    }
}
