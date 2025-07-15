package com.example.musica_be.controller;

import com.example.musica_be.dto.instrumentAnalysis.*;
import com.example.musica_be.service.instrumentAnalysis.InstrumentAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class InstrumentAnalysisController {

    private final InstrumentAnalysisService analysisService;

//    /**
//     * [1] S3 Presigned Upload URL 발급 API
//     * - 강사가 강의 등록 시 S3에 업로드할 수 있도록 사전 URL을 요청
//     * - 프론트에서 S3 업로드 전 이 URL을 먼저 요청해야 함
//     * - 응답 형식:
//     *   {
//     *     "videoUploadUrl": "https://...",
//     *     "videoUrl": "https://...",
//     *     "fileUploadUrl": "https://...",
//     *     "fileUrl": "https://..."
//     *   }
//     */
//    @GetMapping("/url")
//    public ResponseEntity<UrlResponseDto> getPresignedUrl() {
//        return ResponseEntity.ok(analysisService.getUrl());
//    }
//
//    /**
//     * [2] 악기 분석 Job 생성 API
//     * - 프론트가 S3 업로드 완료 후, 다운로드 가능한 Presigned URL을 분석 요청에 사용
//     * - 직접 호출 시: JobCreateRequestDto에 downloadUrl 포함
//     */
//    @PostMapping("/job")
//    public ResponseEntity<JobCreateResponseDto> createJob(@RequestBody JobCreateRequestDto request) {
//        return ResponseEntity.ok(analysisService.createJob(request));
//    }
//
//    /**
//     * [3] 분석 결과 조회 API
//     * - jobId를 이용하여 Music.AI 분석 결과 조회
//     * - 내부 DB에 저장된 InstrumentAnalysis 엔티티 정보를 반환
//     */
//    @GetMapping("/job/{jobId}")
//    public ResponseEntity<InstrumentAnalysisResponseDto> getResult(
//        @PathVariable String jobId,
//        HttpServletRequest request
//    ) {
//        System.out.println("요청된 jobId = " + jobId);
//        System.out.println("요청자 IP = " + request.getRemoteAddr());
//        System.out.println("User-Agent = " + request.getHeader("User-Agent"));
//
//        return ResponseEntity.ok(analysisService.getResult(jobId));
//    }

    /**
     * [4] 전체 통합 분석 요청 API
     * - 강의 ID를 기반으로 Job 생성 및 분석 요청, 결과를 DB에 저장
     * - 분석 결과는 비동기로 추후에 별도 조회해야 함
     *
     * Postman 테스트 예시 요청 바디:
     * {
     *   "lectureId": 1,
     *   "s3DownloadUrl": "https://musica-test-bk.s3.ap-northeast-2.amazonaws.com/..."
     * }
     */
    @PostMapping
    public ResponseEntity<JobCreateResponseDto> requestAnalysis(@RequestBody InstrumentAnalysisRequestDto request) {
        return ResponseEntity.ok(analysisService.requestAnalysis(request));
    }

    /**
     * [5] 분석 결과 갱신 및 감지된 악기 반환 API
     * - jobId를 기반으로 MusicAI에 상태를 조회하고, 결과가 완료되었으면 DB에 반영
     * - 결과 중 true인 악기만 추출하여 리스트로 반환
     *
     * GET /api/analysis/update/{jobId}
     * 응답 예시:
     * {
     *   "detectedInstruments": ["Piano", "Guitar"]
     * }
     */
    @GetMapping("/update/{jobId}")
    public ResponseEntity<AnalysisResultDto> updateAnalysisResult(@PathVariable String jobId) {
        log.info("🔍 분석 결과 업데이트 요청 - jobId: {}", jobId);
        return ResponseEntity.ok(analysisService.updateAnalysisResult(jobId));
    }
}