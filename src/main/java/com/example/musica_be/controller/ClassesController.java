package com.example.musica_be.controller;

import com.example.musica_be.dto.classes.ClassDetailResDto;
import com.example.musica_be.dto.classes.ClassCreateReqDto;
import com.example.musica_be.dto.classes.ClassSummaryDto;
import com.example.musica_be.dto.classes.ClassUpdateReqDto;
import com.example.musica_be.service.classes.ClassesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/instructors/classes") // todo: 수정된 부분
// @RequestMapping("/api/classes") // 원래 코드
public class ClassesController {

    private final ClassesService classesService;

    // 클래스 등록
    @PostMapping
    public ResponseEntity<?> createClass(
            @RequestBody ClassCreateReqDto dto,
            @RequestHeader("Authorization") String jwt
    ) {
        log.info("이게 출력된다면 클래스 컨트롤러까지 들어온 것: ClassesController");
        System.out.println("이게 출력된다면 클래스 컨트롤러까지 들어온 것: ClassesController");
        Long classId = classesService.createClass(dto, jwt);
        return ResponseEntity.status(201).body(Map.of(
                "class_id", classId,
                "message", "클래스가 등록되었습니다."
        ));
    }

    // 클래스 수정
    @PutMapping("/{classId}")
    public ResponseEntity<?> updateClass(
            @PathVariable Long classId,
            @RequestBody ClassUpdateReqDto dto,
            @RequestHeader("Authorization") String jwt
    ) {
        classesService.updateClass(classId, dto, jwt);
        return ResponseEntity.ok().body(Map.of("message", "클래스 수정 완료"));
    }

    // 클래스 삭제
    @DeleteMapping("/{classId}")
    public ResponseEntity<?> deleteClass(
            @PathVariable Long classId,
            @RequestHeader("Authorization") String jwt
    ) {
        classesService.deleteClass(classId, jwt);
        return ResponseEntity.ok().body(Map.of("message", "클래스 삭제 완료"));
    }

    // 클래스 단건 조회 (상세조회)
    @GetMapping("/{classId}")
    public ResponseEntity<ClassDetailResDto> getClassDetail(@PathVariable Long classId) {
        return ResponseEntity.ok(classesService.getClassDetail(classId));
    }

    // 클래스 목록 조회
    @GetMapping
    public ResponseEntity<List<ClassSummaryDto>> getAllClasses() {
        return ResponseEntity.ok(classesService.getAllClasses());
    }
}
