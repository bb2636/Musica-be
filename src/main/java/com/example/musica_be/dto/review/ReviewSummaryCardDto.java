package com.example.musica_be.dto.review;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
@Builder
public class ReviewSummaryCardDto {
    private Long classId;           // 클래스 상세페이지 연결용
    private String classTitle;      // 클래스 제목
    private String levelName;       // 클래스 난이도 요약 ( 예: 초급 / 중급 / 고급 )
    private String summary;         // AI가 요약한 문장 (OPEN AI)
    private String rawComment;      // 실제 후기 내용
    private String maskedUsername;  // 수강생 닉네임 일부 마스킹 (ex: ej*******88)
    private int rating;             // 별점 ( 항상 5점 )

    // 정적 팩토리 메서드
    // 클래스 + 리뷰 + 요약 정보를 하나의 카드 형태로 만들기 위한 생성 메서드
    // 리뷰 객체에서 필요한 값만 꺼내서 카드용 DTO로 변환
    public static ReviewSummaryCardDto from(Review review, String summary, String maskedUsername) {
        Classes cls = review.getClasses();

        Long classId = Optional.ofNullable(cls).map(Classes::getId).orElse(0L);
        String classTitle = Optional.ofNullable(cls).map(Classes::getTitle).orElse("제목 없음");
        String levelName = Optional.ofNullable(cls)
                .map(Classes::getDifficulty)
                .map(Level::getName)
                .orElse("레벨 없음");

        return ReviewSummaryCardDto.builder()
                .classId(classId)
                .classTitle(classTitle)
                .levelName(levelName)
                .summary(summary)
                .rawComment(Optional.ofNullable(review.getComment()).orElse("내용 없음"))
                .maskedUsername(Optional.ofNullable(maskedUsername).orElse("익명"))
                .rating(Optional.ofNullable(review.getRating()).orElse(5))
                .build();
    }
}
