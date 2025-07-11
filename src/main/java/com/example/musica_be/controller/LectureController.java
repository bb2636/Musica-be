package com.example.musica_be.controller;

import com.example.musica_be.dto.lecture.*;
import com.example.musica_be.service.lecture.LectureService;
import com.example.musica_be.util.JwtUtils;
import io.jsonwebtoken.Jwt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class LectureController {

    private final LectureService lectureService;

    // 강의 등록
    @PostMapping("/instructor/classes/{classId}/lectures")
    public ResponseEntity<?> createLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId,
        @RequestBody @Valid LectureCreateReqDto dto
    ) {
        Long lectureId = lectureService.createLecture(jwt, classId, dto);
        return ResponseEntity.status(201).body(Map.of(
            "lecture_id", lectureId,
            "message", "강의가 등록되었습니다."
        ));
    }

    // 강의 수정
    @PutMapping("/instructor/lectures/{lectureId}")
    public ResponseEntity<Map<String, String>> updateLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId,
        @RequestBody @Valid LectureUpdateReqDto dto
    ) {
        lectureService.updateLecture(jwt, lectureId, dto);
        return ResponseEntity.ok(Map.of(
            "message", "강의가 성공적으로 수정되었습니다.",
            "lecture_id", lectureId.toString()
        ));
    }

    // 강의 삭제
    @DeleteMapping("/instructor/lectures/{lectureId}")
    public ResponseEntity<Map<String, String>> deleteLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId
    ) {
        lectureService.deleteLecture(jwt, lectureId);
        return ResponseEntity.ok(Map.of(
            "message", "강의가 정상적으로 삭제되었습니다.",
            "lecture_id", lectureId.toString()
        ));
    }

    // 강의 상세 조회
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<LectureDetailResDto> getLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId
    ) {
        return ResponseEntity.ok(lectureService.getLectureDetail(jwt, lectureId));
    }

    // 강의 목록 조회
    // 1. 비회원 및 공개용 (JWT 없이 접근 가능)
    @GetMapping("/public/classes/{classId}/lectures")
    public ResponseEntity<List<LectureSummaryDto>> getPublicLectureList(
        @PathVariable Long classId
    ) {
        return ResponseEntity.ok(lectureService.getPublicLectureList(classId));
    }

    // 2. 수강생 전용
    @GetMapping("/classes/{classId}/lectures")
    public ResponseEntity<List<LectureSummaryDto>> getLectureList(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId
    ) {
        Long userId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(lectureService.getLectureListForStudent(classId, userId));
    }

    // 3. 강사 전용
    @GetMapping("/instructors/classes/{classId}/lectures")
    public ResponseEntity<List<LectureSummaryDto>> getInstructorLectureList(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId
    ) {
        Long userId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(lectureService.getLectureListForInstructor(classId, userId));
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
    public ResponseEntity<Map<String, Object>> updateLectureOrder(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId,
        @RequestBody @Valid LectureOrderUpdateReqDto dto
    ) {
        List<Long> updatedLectureIds = lectureService.updateLectureOrder(jwt, classId, dto);
        return ResponseEntity.ok(Map.of(
            "message", "강의 순서가 변경되었습니다.",
            "변경 후 강의 아이디", updatedLectureIds
        ));
    }

    // 강의 진행률 변경
    @PatchMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<Void> saveProgress(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId,
        @RequestBody @Valid LectureProgressSaveReqDto dto
    ) {
        lectureService.saveProgress(jwt, lectureId, dto);
        return ResponseEntity.ok().build();
    }

    // 강사가 강의 추가 페이지에서 등록 버튼을 클릭했을 때 실행되는 API
    // 강의 or 파일 업로드 URL 요청
    @GetMapping("/lectures/upload-url")
    public ResponseEntity<?> getUploadUrls(
        @RequestParam(required = false) String videoName,
        @RequestParam(required = false) String fileName
    ) {
        Map<String, String> urls = lectureService.generatePresignedUploadUrls(videoName, fileName);
        return ResponseEntity.ok(urls);
    }

    // 강의 영상 시청을 위한 API
    // 클라이언트에 강의 영상 or 파일 다운로드 url 을 반환
    // 강의 다운로드 URL 요청
    // todo: 테스트용 컨트롤러로 api 테스트 완료 후 주석 처리하거나 삭제해야 함
    @GetMapping("/lectures/view-url")
    public ResponseEntity<?> getDownloadUrl(@RequestParam String key) {
        String url = lectureService.generateDownloadUrl(key);
        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }
}