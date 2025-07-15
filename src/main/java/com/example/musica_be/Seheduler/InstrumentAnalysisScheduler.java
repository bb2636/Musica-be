package com.example.musica_be.Seheduler;

import com.example.musica_be.service.instrumentAnalysis.InstrumentAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentAnalysisScheduler {

    private final InstrumentAnalysisService instrumentAnalysisService;

    @Scheduled(fixedDelay = 60000) // 60초마다 실행
    public void updatePendingJobs() {
        log.info("미완료 분석 Job 상태 갱신 시작");
        instrumentAnalysisService.updatePendingAnalyses();
    }
}
