package com.example.musica_be.repository.classes;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.dto.classes.StudentCountDto;
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
            SELECT new com.example.musica_be.dto.classes.StudentCountDto(pi.classes.id, COUNT(pi))
            FROM PaymentItem pi
            GROUP BY pi.classes.id
        """)
    List<StudentCountDto> countStudentsByClassIds(List<Long> classIds);
}
