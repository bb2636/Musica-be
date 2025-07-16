package com.example.musica_be.service.category;

import com.example.musica_be.domain.classes.Category;
import com.example.musica_be.dto.category.CategoryReqDto;
import com.example.musica_be.repository.classes.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * 📋 모든 카테고리 조회
     */
    public List<Category> getAllCategories() {
        log.info("전체 카테고리 목록 조회");
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 🔍 활성 카테고리만 조회
     */
    public List<Category> getActiveCategories() {
        log.info("활성 카테고리 목록 조회");
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * 🔍 ID로 카테고리 조회
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * ✅ 카테고리 생성
     */
    @Transactional
    public Category createCategory(CategoryReqDto dto) {
        log.info("카테고리 생성 요청: code={}, name={}", dto.getCode(), dto.getName());

        // 중복 코드 체크
        if (dto.getCode() != null && categoryRepository.findByCode(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 코드입니다: " + dto.getCode());
        }

        // 중복 이름 체크
        if (dto.getName() != null && categoryRepository.findByDisplayName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다: " + dto.getName());
        }

        // Category 객체 생성
        Category category = Category.builder()
                .code(dto.getCode() != null ? dto.getCode() : generateCodeFromName(dto.getName()))
                .displayName(dto.getName() != null ? dto.getName() : dto.getDisplayName())
                .displayOrder(dto.getDisplayOrder())
                .isActive(dto.isActive())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("카테고리 생성 성공: id={}, code={}", saved.getId(), saved.getCode());
        return saved;
    }

    /**
     * 🔄 카테고리 수정
     */
    @Transactional
    public Category updateCategory(Long id, CategoryReqDto dto) {
        log.info("카테고리 수정 요청: id={}, code={}, name={}", id, dto.getCode(), dto.getName());

        // 수정할 카테고리 존재 확인
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 카테고리를 찾을 수 없습니다. ID: " + id));

        // 코드 중복 체크 (자기 자신은 허용)
        if (dto.getCode() != null) {
            Optional<Category> duplicateCode = categoryRepository.findByCode(dto.getCode());
            if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
                throw new IllegalArgumentException("다른 카테고리에서 이미 사용 중인 코드입니다: " + dto.getCode());
            }
        }

        // 이름 중복 체크 (자기 자신은 허용)
        String newName = dto.getName() != null ? dto.getName() : dto.getDisplayName();
        if (newName != null) {
            Optional<Category> duplicateName = categoryRepository.findByDisplayName(newName);
            if (duplicateName.isPresent() && !duplicateName.get().getId().equals(id)) {
                throw new IllegalArgumentException("다른 카테고리에서 이미 사용 중인 이름입니다: " + newName);
            }
        }

        // 카테고리 업데이트
        Category updated = Category.builder()
                .id(id)
                .code(dto.getCode() != null ? dto.getCode() : existing.getCode())
                .displayName(newName != null ? newName : existing.getDisplayName())
                .displayOrder(dto.getDisplayOrder())
                .isActive(dto.isActive())
                .build();

        Category saved = categoryRepository.save(updated);
        log.info("카테고리 수정 성공: id={}, code={}", saved.getId(), saved.getCode());
        return saved;
    }

    /**
     * 🗑️ 카테고리 삭제
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("카테고리 삭제 요청: id={}", id);

        // 카테고리 존재 확인
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 카테고리를 찾을 수 없습니다. ID: " + id));

        // TODO: 해당 카테고리를 사용하는 클래스가 있는지 확인
        // if (hasClassesInCategory(id)) {
        //     throw new IllegalStateException("이 카테고리를 사용하는 클래스가 있어 삭제할 수 없습니다.");
        // }

        categoryRepository.deleteById(id);
        log.info("카테고리 삭제 성공: id={}, code={}", id, category.getCode());
    }

    /**
     * 🔄 카테고리 활성/비활성 토글
     */
    @Transactional
    public Category toggleCategoryStatus(Long id) {
        log.info("카테고리 상태 토글 요청: id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + id));

        Category updated = Category.builder()
                .id(category.getId())
                .code(category.getCode())
                .displayName(category.getDisplayName())
                .displayOrder(category.getDisplayOrder())
                .isActive(!category.isActive()) // 상태 토글
                .build();

        Category saved = categoryRepository.save(updated);
        log.info("카테고리 상태 토글 성공: id={}, isActive={}", id, saved.isActive());
        return saved;
    }

    /**
     * 🔧 이름에서 코드 생성 (유틸리티 메서드)
     */
    private String generateCodeFromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "CATEGORY_" + System.currentTimeMillis();
        }

        // 한글 → 영문 변환 (간단한 예시)
        String code = name.trim()
                .replaceAll("피아노", "PIANO")
                .replaceAll("기타", "GUITAR")
                .replaceAll("드럼", "DRUM")
                .replaceAll("보컬", "VOCAL")
                .replaceAll("바이올린", "VIOLIN")
                .replaceAll("\\s+", "_")
                .toUpperCase();

        // 영문, 숫자, 언더스코어만 허용
        code = code.replaceAll("[^A-Z0-9_]", "");

        return code.isEmpty() ? "CATEGORY_" + System.currentTimeMillis() : code;
    }

    // TODO: 클래스 연관성 체크 메서드 (나중에 구현)
    // private boolean hasClassesInCategory(Long categoryId) {
    //     return classRepository.existsByCategoryId(categoryId);
    // }
}