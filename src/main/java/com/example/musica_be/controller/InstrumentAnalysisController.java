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

    /**
     * 전체 통합 분석 요청 API
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
     * 분석 결과 갱신 및 감지된 악기 반환 API
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