package com.example.musica_be.controller;

import com.example.musica_be.dto.lecture.*;
import com.example.musica_be.service.lecture.LectureService;
import com.example.musica_be.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class LectureController {

    private final LectureService lectureService;

    // 강의 등록
    @PostMapping("/instructors/classes/{classId}/lectures")
    public ResponseEntity<LectureCreateResDto> createLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId,
        @RequestBody @Valid LectureCreateReqDto dto
    ) {
        LectureCreateResDto response = lectureService.createLecture(jwt, classId, dto);
        System.out.println("🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉response🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉 = " + response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 강의 수정
    @PutMapping("/instructors/lectures/{lectureId}")
    public ResponseEntity<Map<String, String>> updateLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId,
        @RequestBody @Valid LectureUpdateReqDto dto
    ) {
        lectureService.updateLecture(jwt, lectureId, dto);
        return ResponseEntity.ok(Map.of(
            "message", "강의 수정 완료",
            "lecture_id", lectureId.toString()
        ));
    }

    // 강의 삭제
    @DeleteMapping("/instructors/lectures/{lectureId}")
    public ResponseEntity<Map<String, String>> deleteLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId
    ) {
        lectureService.deleteLecture(jwt, lectureId);
        return ResponseEntity.ok(Map.of(
            "message", "강의 삭제 완료",
            "lecture_id", lectureId.toString()
        ));
    }

    // 강의 상세 조회 (시청 정보 및 강의 세부 정보 포함)
    // - 사용자가 선택한 강의에 대한 상세 정보 + 시청 정보(watchedSeconds, isCompleted)를 함께 반환
    // - 영상 재생을 위한 videoUrl, 강의 자료 다운로드 URL(fileUrl)도 포함
    // - 프론트의 시청 페이지(새 창/모달) 구성 시 필요한 모든 정보를 한번에 제공
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<LectureDetailResDto> getLecture(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long lectureId
    ) {
        return ResponseEntity.ok(lectureService.getLectureDetail(jwt, lectureId));
    }

    // 강의 목록 조회
    // 1. 비회원 및 공개용 (JWT 없이 접근 가능)
    @GetMapping("/classes/{classId}/lectures")
    public ResponseEntity<List<LectureSummaryDto>> getLectureList(
        @PathVariable Long classId
    ) {
        return ResponseEntity.ok(lectureService.getLectureList(classId));
    }

    // 2. 수강생 전용
    @GetMapping("/users/classes/{classId}/lectures")
    public ResponseEntity<List<LectureSummaryDto>> getLectureListForStudent(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId
    ) {
        Long userId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(lectureService.getLectureListForStudent(classId, userId));
    }

    // 3. 강사 전용
    @GetMapping("/instructors/classes/{classId}/lectures")
    public ResponseEntity<List<LectureSummaryDto>> getLectureListForInstructor(
        @RequestHeader("Authorization") String jwt,
        @PathVariable Long classId
    ) {
        Long userId = JwtUtils.extractUserId(jwt);
        return ResponseEntity.ok(lectureService.getLectureListForInstructor(classId, userId));
    }

    // 강의 순서 변경
    @PatchMapping("/instructors/classes/{classId}/lectures/order")
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
        String url = lectureService.generatePresignedDownloadUrl(key);
        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }
}