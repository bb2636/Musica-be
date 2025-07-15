package com.example.musica_be.domain.instrumentAnalysis;

/**
 * 악기 분석 요청 상태
 * - PENDING: 분석 요청됨
 * - SUCCEEDED: 분석 완료됨 (외부 API 응답값 기준)
 * - FAILED: 실패 또는 오류 발생
 */
public enum AnalysisStatus {
    PENDING,
    SUCCEEDED,
    FAILED
}
