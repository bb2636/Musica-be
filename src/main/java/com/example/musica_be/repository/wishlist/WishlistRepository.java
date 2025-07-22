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

    // ✅ 특정 유저가 특정 클래스를 찜했는지 여부 확인
    boolean existsByUserAndClasses(User user, Classes classes);

    // ✅ 특정 유저와 클래스 조합의 찜 엔티티 조회 (Optional 반환)
    Optional<Wishlist> findByUserAndClasses(User user, Classes classes);

    // ✅ 특정 유저가 찜한 모든 클래스 목록 조회 (User 엔티티 기준)
    List<Wishlist> findAllByUser(User user);

    // ✅ 특정 유저 ID로 찜 목록 조회 (User ID 기준)
    List<Wishlist> findByUserId(Long userId);

    // ✅ 특정 클래스의 찜 개수(count)를 반환 (단건)
    int countByClasses(Classes classes);

    // ✅ 클래스 목록(classIds)에 대해 찜 수만 통계로 한 번에 조회 (ClassCardStatisticsDto로 매핑)
    @Query("""
    SELECT new com.example.musica_be.dto.classes.ClassCardStatisticsDto(w.classes.id, 0L, COUNT(w), 0.0, 0L)
    FROM Wishlist w
    WHERE w.classes.id IN :classIds
    GROUP BY w.classes.id
    """)
    List<ClassCardStatisticsDto> getWishlistCounts(@Param("classIds") List<Long> classIds);

    // ✅ 특정 클래스 ID 기준으로 모든 찜 엔티티 삭제 (관리자 기능 등에서 사용)
    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.classes.id = :classId")
    void deleteByClassId(@Param("classId") Long classId);

    // ✅ 특정 유저가 찜한 클래스들에 대한 찜 수 통계를 한 번에 조회 (userId 기준)
    // -> 프론트에서 찜 수 UI 빠르게 반영할 때 활용
    @Query("""
    SELECT w.classes.id AS classId, COUNT(w) AS cnt
    FROM Wishlist w
    WHERE w.user.id = :userId
    GROUP BY w.classes.id
    """)
    List<Object[]> getWishlistCountsForUser(@Param("userId") Long userId);
}
