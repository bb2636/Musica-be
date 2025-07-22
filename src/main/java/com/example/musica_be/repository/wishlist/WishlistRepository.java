package com.example.musica_be.repository.wishlist;

import com.example.musica_be.domain.Wishlist;
import com.example.musica_be.domain.classes.Classes;
import com.example.musica_be.domain.user.User;
import com.example.musica_be.dto.classes.ClassCardStatisticsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    boolean existsByUserAndClasses(User user, Classes classes);
    Optional<Wishlist> findByUserAndClasses(User user, Classes classes);
    List<Wishlist> findAllByUser(User user);

    List<Wishlist> findByUserId(Long userId);

    int countByClasses(Classes classes);  // 찜 수

    //  ClassCardStatisticsDto용 통합 통계 쿼리
    @Query("""
    SELECT new com.example.musica_be.dto.classes.ClassCardStatisticsDto(w.classes.id, 0L, COUNT(w), 0.0, 0L)
    FROM Wishlist w
    WHERE w.classes.id IN :classIds
    GROUP BY w.classes.id
    """)
    List<ClassCardStatisticsDto> getWishlistCounts(@Param("classIds") List<Long> classIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.classes.id = :classId")
    void deleteByClassId(@Param("classId") Long classId);
}
