package com.example.musica_be.controller;

import com.example.musica_be.dto.category.CategoryResDto;
import com.example.musica_be.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instructors/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

  private final CategoryService categoryService;

  /**
   * 전체 카테고리 목록 조회
   */
  @GetMapping
  public List<CategoryResDto> getAllCategories() {
    return categoryService.getAllCategoriesResDto();
  }
}
