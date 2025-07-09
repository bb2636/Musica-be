package com.example.musica_be.dto.instrumentAnalysis;

import com.example.musica_be.domain.instrumentAnalysis.AnalysisStatus;
import com.example.musica_be.domain.instrumentAnalysis.InstrumentAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentAnalysisResponseDto {
    private Long id;
    private Long lectureId;
    private String jobId;
    private String inputFileUrl;
    private String detectedInstruments;
    private String confidenceScores;
    private AnalysisStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    public static InstrumentAnalysisResponseDto from(InstrumentAnalysis analysis) {
        return InstrumentAnalysisResponseDto.builder()
            .id(analysis.getId())
            .lectureId(analysis.getLecture().getId())
            .jobId(analysis.getJobId())
            .inputFileUrl(analysis.getInputFileUrl())
            .detectedInstruments(analysis.getDetectedInstruments())
            .confidenceScores(analysis.getConfidenceScores())
            .status(analysis.getStatus())
            .requestedAt(analysis.getRequestedAt())
            .completedAt(analysis.getCompletedAt())
            .build();
    }
}
