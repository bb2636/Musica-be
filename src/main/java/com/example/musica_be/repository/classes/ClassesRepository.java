package com.example.musica_be.repository.classes;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.dto.classes.ClassesStudentCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Long> {
    List<Classes> findByTitleContainingAndDifficulty_IdAndCategory(String title, Long difficultyId, Category category);

    @Query("""
            SELECT DISTINCT pi.classes
            FROM PaymentItem pi
            JOIN pi.payment p
            WHERE p.user.id = :userId
              AND pi.paymentStatus.name <> 'CANCELED'
        """)
    List<Classes> findEnrolledClassesByUserId(@Param("userId") Long userId);

    List<Classes> findByInstructorId(Long userId);

    @Query("""
            SELECT c FROM Classes c
            WHERE (:keyword IS NULL OR c.title LIKE %:keyword%)
              AND (:categoryId IS NULL OR c.category.id = :categoryId)
              AND (:difficultyId IS NULL OR c.difficulty.id = :difficultyId)
        """)
    List<Classes> findByConditions(@Param("keyword") String keyword,
                                   @Param("categoryId") Long categoryId,
                                   @Param("difficultyId") Long difficultyId);

    @Query("""
            SELECT new com.example.musica_be.dto.classes.ClassesStudentCountDto(pi.classes.id, COUNT(pi))
            FROM PaymentItem pi
            GROUP BY pi.classes.id
        """)
    List<ClassesStudentCountDto> countStudentsByClassIds(List<Long> classIds);

    // 유저의 levelId 기준 추천 클래스 (별점 평균 내림차순)
    @Query("""
    SELECT c FROM Classes c
    WHERE c.difficulty.id = :levelId
    ORDER BY
        (SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.classes.id = c.id) DESC
    """)
    List<Classes> findRecommendedByLevelId(@Param("levelId") Long levelId, Pageable pageable);

    // 최신 클래스(20 limit 내림차순정렬)
    List<Classes> findTop20ByOrderByCreatedAtDesc();

    // 무료 클래스 5개 조회 (최신순)
    List<Classes> findTop5ByClassPriceOrderByCreatedAtDesc(Integer classPrice);

    // 검색 결과 페이징
    @Query("""
            SELECT c FROM Classes c
            WHERE (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.category.id = :categoryId)
              AND (:difficultyId IS NULL OR c.difficulty.id = :difficultyId)
        """)
    Page<Classes> searchFiltered(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("difficultyId") Long difficultyId,
        Pageable pageable
    );
}
