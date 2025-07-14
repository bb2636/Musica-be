package com.example.musica_be.controller;

import com.example.musica_be.dto.classes.ClassCardDto;
import com.example.musica_be.service.classes.ClassesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainPageController {
    private final ClassesService classesService;

    // 추천 클래스 조회
    @GetMapping("/recommend")
    public ResponseEntity<List<ClassCardDto>> getRecommendedClasses(
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        return ResponseEntity.ok(classesService.getRecommendedClasses(jwt));
    }

    // 인기 클래스 조회
    @GetMapping("/popular")
    public ResponseEntity<List<ClassCardDto>> getPopularClasses() {
        return ResponseEntity.ok(classesService.getPopularClasses());
    }

    // 최신 클래스 조회
    @GetMapping("/latest")
    public ResponseEntity<List<ClassCardDto>> getLatestClasses() {
        return ResponseEntity.ok(classesService.getLatestClasses());
    }

}
