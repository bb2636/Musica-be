package com.example.musica_be.repository.classes;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.classes.Classes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Long> {
    List<Classes> findByTitleContainingAndDifficulty_IdAndCategory(String title, Long difficultyId, Category category);

    // 관리자가 추천으로 지정한 클래스 (isRecommended = true) 중 최신순 상위 4개
    List<Classes> findTop4ByIsRecommendedTrueOrderByCreatedAtDesc();

    // 추천이 아닌 클래스 중 최신순 (isRecommended = false)
    List<Classes> findByIsRecommendedFalseOrderByCreatedAtDesc();

    // 최신 클래스(16 limit 정렬)
    List<Classes> findTop16ByOrderByCreatedAtDesc();
}
