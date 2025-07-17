package com.example.musica_be.repository.review;

import com.example.musica_be.domain.Review;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.classes.ClassCardStatisticsDto;
import com.example.musica_be.dto.classes.ClassesRatingAvgDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 마이페이지 조회용(본인 작성 후기 전체 조회)
    List<Review> findAllByUser(User user);

    // 수정/삭제 전 본인 확인용
    Optional<Review> findByReviewIdAndUser(Integer reviewId, User user);

    // 마이페이지 후기 목록 조회 (본인 후기 목록 조회)
    List<Review> findByUserId(Long userId);

    // 클래스 ID별 평균 별점 목록 반환 (ClassCard 목록용) (여러개 클래스 별점순 필터링)
    // - classIds 파라미터는 현재 쿼리에 사용되지 않음 (필요하면 WHERE IN 추가 필요)
    @Query("""
        SELECT new com.example.musica_be.dto.classes.ClassesRatingAvgDto(r.classes.id, AVG(r.rating))
        FROM Review r
        GROUP BY r.classes.id
        """)
    List<ClassesRatingAvgDto> getAverageRatingsByClassIds(List<Long> classIds);

    // 단일 클래스 ID의 평균 별점 (상세페이지 용도) - (FE) ClassCard 표시용
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.classes.id = :classId")
    Optional<Double> calculateAverageRatingByClassId(@Param("classId") Long classId);

    // 5점 리뷰 중 최신순 6건 (user + class fetch 포함) - 메인 AI 카드용
    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.user u
        JOIN FETCH r.classes c
        WHERE r.rating = 5
        ORDER BY r.createdAt DESC
        """)
    List<Review> findTop6ByRatingIsFiveOrderByCreatedAtDesc(Pageable pageable);

    // ReviewRepository 통계 쿼리
    @Query("""
    SELECT new com.example.musica_be.dto.classes.ClassCardStatisticsDto(
        r.classes.id, 0L, 0L, AVG(r.rating), COUNT(r)
    )
    FROM Review r
    WHERE r.classes.id IN :classIds
    GROUP BY r.classes.id
    """)
    List<ClassCardStatisticsDto> getAvgRatings(@Param("classIds") List<Long> classIds);

    @Query("""
            SELECT new com.example.musica_be.dto.classes.ClassesRatingAvgDto(r.classes.id, AVG(r.rating))
            FROM Review r
            WHERE r.classes.id IN :classIds
            GROUP BY r.classes.id
        """)
    List<ClassesRatingAvgDto> getAverageRatings(@Param("classIds") List<Long> classIds);

    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.classes.instructor.id = :instructorId
        """)
    int countByInstructorId(@Param("instructorId") Long instructorId);

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0.0)
            FROM Review r
            WHERE r.classes.instructor.id = :instructorId
        """)
    double averageRatingByInstructorId(@Param("instructorId") Long instructorId);

    List<Review> findTop3ByClasses_Instructor_IdOrderByCreatedAtDesc(Long instructorId);

    Page<Review> findByClassesIn(List<Classes> classes, Pageable pageable);
}
