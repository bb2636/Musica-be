package com.example.musica_be.repository.classes;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.classes.Classes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Long> {
    List<Classes> findByTitleContainingAndDifficulty_IdAndCategory(String title, Long difficultyId, Category category);
}
