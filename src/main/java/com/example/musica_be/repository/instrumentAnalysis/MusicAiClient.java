package com.example.musica_be.repository.instrumentAnalysis;

import com.example.musica_be.dto.instrumentAnalysis.JobCreateRequestDto;
import com.example.musica_be.dto.instrumentAnalysis.JobCreateResponseDto;
import com.example.musica_be.dto.instrumentAnalysis.JobStatusResponseDto;
import com.example.musica_be.dto.instrumentAnalysis.UrlResponseDto;

public interface MusicAiClient {
    // 업로드된 파일에 대해 악기 분석 작업(Job) 생성 요청
    JobCreateResponseDto createJob(JobCreateRequestDto request);
    // 해당 job 아이디의 결과를 가져옴 (분석 결과 true 인 악기들의 이름을 json 형식으로 응답받음)
    JobStatusResponseDto getJobResult(String jobId);
}
