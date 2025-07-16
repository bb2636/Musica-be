package com.example.musica_be.repository.classes;

import com.example.musica_be.domain.classes.Category;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCode(String code);
    /**
     * 📋 모든 카테고리를 displayOrder 순으로 조회
     */
    List<Category> findAllByOrderByDisplayOrderAsc();

    /**
     * 🔍 활성 카테고리만 displayOrder 순으로 조회
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 🔍 displayName으로 카테고리 조회
     */
    Optional<Category> findByDisplayName(String displayName);

    /**
     * 📊 활성 카테고리 수 조회
     */
    long countByIsActive(boolean isActive);

    /**
     * 📊 전체 카테고리 중 활성 카테고리 비율 계산용
     */
    long countByIsActiveTrue();

    /**
     * 🔍 displayOrder로 카테고리 존재 여부 확인
     */
    boolean existsByDisplayOrder(int displayOrder);
}
