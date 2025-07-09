package com.example.musica_be.controller;

import com.example.musica_be.dto.instrumentAnalysis.*;
import com.example.musica_be.service.instrumentAnalysis.InstrumentAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class InstrumentAnalysisController {

    private final InstrumentAnalysisService analysisService;

    // 1. Presigned URL 발급 (GCS 업로드용)
    @GetMapping("/url")
    public ResponseEntity<UrlResponseDto> getUrl() {
        return ResponseEntity.ok(analysisService.getUrl());
    }

    // 2. 악기 분석 Job 생성
    @PostMapping("/job")
    public ResponseEntity<JobCreateResponseDto> createJob(@RequestBody JobCreateRequestDto request) {
        return ResponseEntity.ok(analysisService.createJob(request));
    }

    // 3. 분석 결과 조회
    @GetMapping("/job/{jobId}")
    public ResponseEntity<InstrumentAnalysisResponseDto> getResult(
        @PathVariable String jobId,
        HttpServletRequest request
    ) {
        System.out.println("요청된 jobId = " + jobId);
        System.out.println("요청자 IP = " + request.getRemoteAddr());
        System.out.println("User-Agent = " + request.getHeader("User-Agent"));

        return ResponseEntity.ok(analysisService.getResult(jobId));
    }

    // 4. 전체 통합 요청: 강의 조회 + Job 생성 + 분석 결과 DB 저장
    @PostMapping
    public ResponseEntity<JobCreateResponseDto> requestAnalysis(@RequestBody InstrumentAnalysisRequestDto request) {
        return ResponseEntity.ok(analysisService.requestAnalysis(request));
    }

    // 5.
}
