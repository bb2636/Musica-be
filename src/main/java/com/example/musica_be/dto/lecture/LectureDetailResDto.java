package com.example.musica_be.dto.lecture;

import com.example.musica_be.domain.instrumentAnalysis.InstrumentAnalysis;
import com.example.musica_be.domain.lecture.Lecture;
import com.example.musica_be.domain.lecture.LectureProgress;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Getter
@Builder
public class LectureDetailResDto {
    private Long id;
    private String title;
    private String videoUrl;
    private String fileUrl;
    private Integer lectureOrder;
    private Integer duration;
    private Integer progress;
    private Boolean isCompleted;
    private Long classId;

    private Map<String, Boolean> detectedInstruments;
    private Map<String, Double> confidenceScores;
    private Map<String, Double> thresholds;

    private static final ObjectMapper objectMapper = new ObjectMapper(); // ✅ ObjectMapper 추가

    public static LectureDetailResDto from(
        Lecture lecture,
        LectureProgress progress,
        String videoUrl,
        String fileUrl,
        InstrumentAnalysis analysis
    ) {
        Map<String, Boolean> instrumentsMap = null;
        Map<String, Double> confidenceMap = null;
        Map<String, Double> thresholdsMap = null;

        try {
            if (analysis != null) {
                if (analysis.getDetectedInstruments() != null) {
                    instrumentsMap = objectMapper.readValue(
                        analysis.getDetectedInstruments(),
                        new TypeReference<>() {}
                    );
                }
                if (analysis.getConfidenceScores() != null) {
                    confidenceMap = objectMapper.readValue(
                        analysis.getConfidenceScores(),
                        new TypeReference<>() {}
                    );
                }
                if (analysis.getThresholds() != null) {
                    thresholdsMap = objectMapper.readValue(
                        analysis.getThresholds(),
                        new TypeReference<>() {}
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("악기 분석 결과 파싱 실패", e);
        }

        return LectureDetailResDto.builder()
            .id(lecture.getId())
            .title(lecture.getTitle())
            .videoUrl(videoUrl)
            .fileUrl(fileUrl)
            .lectureOrder(lecture.getLectureOrder())
            .duration(lecture.getDuration())
            .progress(progress != null ? progress.getWatchedSeconds() : 0)
            .isCompleted(progress != null && Boolean.TRUE.equals(progress.getIsCompleted()))
            .classId(lecture.getClasses().getId())
            .detectedInstruments(instrumentsMap)
            .confidenceScores(confidenceMap)
            .thresholds(thresholdsMap)
            .build();
    }

}