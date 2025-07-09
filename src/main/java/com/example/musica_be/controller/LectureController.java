package com.example.musica_be.controller;

import com.example.musica_be.dto.lecture.*;
import com.example.musica_be.service.lecture.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    // 강의 등록
    @PostMapping("/classes/{classId}/lectures")
    public ResponseEntity<Long> createLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId,
        @RequestBody @Valid LectureCreateReqDto dto
    ) {
        Long lectureId = lectureService.createLecture(jwt, classId, dto);
        return ResponseEntity.status(201).body(lectureId);
    }

    // 강의 수정
    @PutMapping("/lectures/{lectureId}")
    public ResponseEntity<Void> updateLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId,
        @RequestBody @Valid LectureUpdateReqDto dto
    ) {
        lectureService.updateLecture(jwt, lectureId, dto);
        return ResponseEntity.ok().build();
    }

    // 강의 삭제
    @DeleteMapping("/lectures/{lectureId}")
    public ResponseEntity<Void> deleteLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId
    ) {
        lectureService.deleteLecture(jwt, lectureId);
        return ResponseEntity.ok().build();
    }

    // 강의 상세 조회
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<LectureDetailResDto> getLecture(@PathVariable Long lectureId) {
        return ResponseEntity.ok(lectureService.getLectureDetail(lectureId));
    }

    // 강의 목록 조회
    @GetMapping("/classes/{classId}/lectures")
    public ResponseEntity<List<LectureSummaryDto>> getLectureList(@PathVariable Long classId) {
        return ResponseEntity.ok(lectureService.getLectureList(classId));
    }

    // 강의 시청
    @GetMapping("/lectures/{lectureId}/watch")
    public ResponseEntity<LectureWatchResDto> watchLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId
    ) {
        return ResponseEntity.ok(lectureService.watchLecture(jwt, lectureId));
    }

    // 강의 순서 변경
    @PatchMapping("/classes/{classId}/lectures/order")
    public ResponseEntity<Void> updateLectureOrder(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId,
        @RequestBody @Valid LectureOrderUpdateReqDto dto
    ) {
        lectureService.updateLectureOrder(jwt, classId, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<Void> saveProgress(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId,
        @RequestBody @Valid LectureProgressSaveReqDto dto
    ) {
        lectureService.saveProgress(jwt, lectureId, dto);
        return ResponseEntity.ok().build();
    }
}