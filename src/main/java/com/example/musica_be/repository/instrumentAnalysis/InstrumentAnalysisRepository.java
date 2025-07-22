package com.example.musica_be.repository.instrumentAnalysis;

import com.example.musica_be.domain.instrumentAnalysis.AnalysisStatus;
import com.example.musica_be.domain.instrumentAnalysis.InstrumentAnalysis;
import com.example.musica_be.domain.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface InstrumentAnalysisRepository extends JpaRepository<InstrumentAnalysis, Long> {
    // findByJobId(String jobId) : Music.AI 완료 응답 수신 시 job 아이디로 결과를 찾기 위함
    Optional<InstrumentAnalysis> findByJobId(String jobId);

    // existsByLectureId(Long lectureId) : 중복 분석 방지
    boolean existsByLectureId(Long lectureId);

    List<InstrumentAnalysis> findByStatus(AnalysisStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM InstrumentAnalysis ia WHERE ia.lecture.id = :lectureId")
    void deleteByLectureId(@Param("lectureId") Long lectureId);

    Optional<InstrumentAnalysis> findByLecture(Lecture lecture);
}
