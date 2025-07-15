package com.example.musica_be.controller;

import com.example.musica_be.dto.classes.ClassDetailResDto;
import com.example.musica_be.dto.classes.ClassCreateReqDto;
import com.example.musica_be.dto.classes.ClassSummaryDto;
import com.example.musica_be.dto.classes.ClassUpdateReqDto;
import com.example.musica_be.service.classes.ClassesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class ClassesController {

    private final ClassesService classesService;

    // 클래스 등록
    @PostMapping("/instructors/classes")
    public ResponseEntity<?> createClass(
        @RequestHeader("Authorization") String jwt,
        @RequestBody @Valid ClassCreateReqDto dto
        ) {
        Long classId = classesService.createClass(dto, jwt);
        return ResponseEntity.status(201).body(Map.of(
            "class_id", classId,
            "message", "클래스 등록 완료"
        ));
    }

    // 클래스 수정
    @PutMapping("/instructors/classes/{classId}")
    public ResponseEntity<?> updateClass(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId,
        @RequestBody @Valid ClassUpdateReqDto dto
    ) {
        classesService.updateClass(classId, dto, jwt);
        return ResponseEntity.ok().body(Map.of(
            "message", "클래스 수정 완료",
            "class_id", classId.toString()
        ));
    }

    // 클래스 삭제
    @DeleteMapping("/instructors/classes/{classId}")
    public ResponseEntity<Map<String, String>> deleteClass(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId
    ) {
        classesService.deleteClass(classId, jwt);
        return ResponseEntity.ok().body(Map.of(
            "message", "클래스 삭제 완료",
            "class_id", classId.toString()
        ));
    }

    // 클래스 상세 조회
    @GetMapping("/classes/{classId}")
    public ResponseEntity<ClassDetailResDto> getClassDetail(
        @RequestHeader(value = "Authorization", required = false) String jwt,
        @PathVariable Long classId
    ) {
        return ResponseEntity.ok(classesService.getClassDetail(jwt, classId));
    }

    // 클래스 목록 조회
    // 1. 클래스 검색 결과 - 비회원 및 공개용 (JWT 없이 접근 가능)
    // 지원 정렬 기준:
    //  - popular     : 조회수 내림차순 (인기순)
    //  - priceAsc    : 가격 낮은 순
    //  - priceDesc   : 가격 높은 순
    //  - latest      : 최근 등록 순
    //  - students    : 수강생 많은 순
    //  - rating      : 별점 높은 순
    @GetMapping("/classes")
    public ResponseEntity<List<ClassSummaryDto>> getClassList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long difficultyId,
        @RequestParam(required = false, defaultValue = "latest") String sort // 예: "latest", "popular", "priceAsc", "students", "rating"
    ) {
        // GET /api/classes?sort=popular,priceAsc 같이 sort 조건이 여러 개 있을 때,
        // 문자열을 파싱해서 다중 정렬을 처리하기 위해 sortList 를 만들어 서비스 단으로 넘겨줌
        List<String> sortList = Arrays.stream(sort.split(","))
            .map(String::trim)
            .toList(); // Java 16 이상

        List<ClassSummaryDto> result = classesService.searchFilteredClassList(keyword, categoryId, difficultyId, sortList);
        return ResponseEntity.ok(result);
    }

    // 2. 수강생 전용
    @GetMapping("/users/classes")
    public ResponseEntity<List<ClassSummaryDto>> getClassListForStudent(
        @RequestHeader("Authorization") String jwt
    ) {
        return ResponseEntity.ok(classesService.getClassListForStudent(jwt));
    }

    // 3. 강사 전용
    @GetMapping("/instructors/classes")
    public ResponseEntity<List<ClassSummaryDto>> getClassListForInstructor(
        @RequestHeader("Authorization") String jwt
    ) {
        return ResponseEntity.ok(classesService.getClassListForInstructor(jwt));
    }
}