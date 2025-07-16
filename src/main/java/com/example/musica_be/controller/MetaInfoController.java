package com.example.musica_be.controller;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.domain.user.Level;
import com.example.musica_be.repository.classes.CategoryRepository;
import com.example.musica_be.repository.user.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meta")
public class MetaInfoController {

    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;

    /**
     * 활성화된 카테고리 목록 (정렬순 정렬)
     */
    @GetMapping("/categories")
    public List<Category> getActiveCategories() {
        return categoryRepository.findAll().stream()
            .filter(Category::isActive)
            .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
            .toList();
    }

    /**
     * 전체 난이도(Level) 목록
     */
    @GetMapping("/levels")
    public List<Level> getLevels() {
        return levelRepository.findAll();
    }
}
