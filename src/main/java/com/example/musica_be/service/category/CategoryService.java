package com.example.musica_be.service.category;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.dto.category.CategoryReqDto;
import com.example.musica_be.repository.classes.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category createCategory(CategoryReqDto dto) {
        // 중복 코드 존재시 예외
        if (categoryRepository.findByCode(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 코드입니다.");
        }

        // Category 객체 생성
        Category category = Category.builder()
                .code(dto.getCode())
                .displayName(dto.getDisplayName())
                .displayOrder(dto.getDisplayOrder())
                .isActive(dto.isActive())
                .build();

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, CategoryReqDto dto) {
        // 1. 수정할 카테고리가 실제 존재하는지 확인
        categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 카테고리를 찾을 수 없습니다."));
        // 2. 코드 중복 체크 (단, 자기 자신은 허용)
        Optional<Category> duplicate = categoryRepository.findByCode(dto.getCode());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new IllegalArgumentException("다른 카테고리에서 이미 사용 중인 코드입니다.");
        }

        // 3. 새 Entity 생성 (불변성 유지 + 사이드이펙트 최소화)
        Category updated = Category.builder()
                .id(id)
                .code(dto.getCode())
                .displayName(dto.getDisplayName())
                .displayOrder(dto.getDisplayOrder())
                .isActive(dto.isActive())
                .build();

        return categoryRepository.save(updated);
    }

}
