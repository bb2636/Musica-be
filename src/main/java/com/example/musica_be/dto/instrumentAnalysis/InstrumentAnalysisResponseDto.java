package com.example.musica_be.dto.instrumentAnalysis;

import com.example.musica_be.domain.instrumentAnalysis.AnalysisStatus;
import com.example.musica_be.domain.instrumentAnalysis.InstrumentAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * InstrumentAnalysis 엔티티를 기반으로
 * 분석 결과를 클라이언트에 반환할 때 사용하는 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentAnalysisResponseDto {

    /** 분석 고유 ID */
    private Long id;

    /** 분석 대상 강의 ID */
    private Long lectureId;

    /** 외부 Music.AI 분석 Job ID */
    private String jobId;

    /** S3 다운로드 URL (입력 데이터) */
    private String videoUrl;

    /** 감지된 악기 목록 (JSON string of Map<String, Boolean>) */
    private String detectedInstruments;

    /** 각 악기의 신뢰도 점수 (JSON string of Map<String, Double>) */
    private String confidenceScores;

    /** 각 악기의 임계값 (thresholds, JSON string of Map<String, Double>) */
    private String thresholds;

    /** 분석 상태: PENDING, SUCCEEDED, FAILED */
    private AnalysisStatus status;

    /** 분석 요청 시각 */
    private LocalDateTime requestedAt;

    /** 분석 완료 시각 */
    private LocalDateTime completedAt;

    /**
     * InstrumentAnalysis 엔티티를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static InstrumentAnalysisResponseDto from(InstrumentAnalysis analysis) {
        return InstrumentAnalysisResponseDto.builder()
            .id(analysis.getId())
            .lectureId(analysis.getLecture().getId())
            .jobId(analysis.getJobId())
            .videoUrl(analysis.getVideoUrl())
            .detectedInstruments(analysis.getDetectedInstruments())
            .confidenceScores(analysis.getConfidenceScores())
            .thresholds(analysis.getThresholds()) // ← 누락되었던 필드 추가
            .status(analysis.getStatus())
            .requestedAt(analysis.getRequestedAt())
            .completedAt(analysis.getCompletedAt())
            .build();
    }
}