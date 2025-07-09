package com.example.musica_be.domain.instrumentAnalysis;

import com.example.musica_be.domain.lecture.Lecture;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InstrumentAnalysis {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 어떤 강의의 분석 결과인지
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lecture_id", nullable = false)
  private Lecture lecture;

  // 외부 API(Music.AI)의 job id
  @Column(nullable = false, unique = true)
  private String jobId;

  // 변환된 오디오(GCS) 주소
  private String inputFileUrl;

  // ["vocals", "guitar"] 와 같은 악기 리스트 (문자열 배열 혹은 JSON 문자열)
  @Lob
  private String detectedInstruments;

  // {"piano": 0.95, "vocals": 0.88} 와 같은 확률 정보
  @Lob
  private String confidenceScores;

  // PENDING, SUCCESS, FAILED
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AnalysisStatus status;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime requestedAt;

  @Column
  private LocalDateTime completedAt;
}