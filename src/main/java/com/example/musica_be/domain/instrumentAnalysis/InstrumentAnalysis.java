package com.example.musica_be.domain.instrumentAnalysis;

import com.example.musica_be.domain.lecture.Lecture;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 악기 분석 결과를 저장하는 엔티티
 * - 강의 영상 업로드 후 Music.AI API에 분석을 요청하고,
 *   분석 결과(instruments, probabilities, thresholds)를 저장함
 * - 분석은 비동기 방식으로 동작하며, 별도 API로 결과를 조회해 DB에 업데이트
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 외부 Music.AI에서 생성된 Job ID (UUID) */
  private String jobId;

  /** 분석 대상이 되는 S3 Presigned 다운로드 URL */
  @Column(name = "videoUrl", length = 2048)
  private String videoUrl;

  /** 분석 상태: PENDING, SUCCEEDED, FAILED */
  @Enumerated(EnumType.STRING)
  private AnalysisStatus status;

  /** 분석 요청 시간 */
  private LocalDateTime requestedAt;

  /** 분석 완료 시간 */
  private LocalDateTime completedAt;

  /** 감지된 악기 정보 (예: {"piano": true, "drums": false, ...}) */
  @Lob
  private String detectedInstruments; // JSON string of boolean map

  /** 각 악기의 confidence score (예: {"piano": 0.95, ...}) */
  @Lob
  private String confidenceScores; // JSON string of double map

  /** 각 악기에 대한 임계값 (threshold) (예: {"piano": 0.91, ...}) */
  @Lob
  private String thresholds; // JSON string of double map

  /** 분석이 연결된 강의 */
  @ManyToOne(fetch = FetchType.LAZY)
  private Lecture lecture;

  /**
   * 분석 성공 시 필드 업데이트
   * @param detectedJson 감지된 악기 결과 (Boolean Map → JSON)
   * @param scoreJson 악기별 confidence score (Double Map → JSON)
   * @param thresholdJson 악기별 threshold 값 (Double Map → JSON)
   * @param completedAt 분석 완료 시각
   */
  public void updateSuccess(String detectedJson, String scoreJson, String thresholdJson, LocalDateTime completedAt) {
    this.detectedInstruments = detectedJson;
    this.confidenceScores = scoreJson;
    this.thresholds = thresholdJson;
    this.status = AnalysisStatus.SUCCEEDED;
    this.completedAt = completedAt;
  }
}