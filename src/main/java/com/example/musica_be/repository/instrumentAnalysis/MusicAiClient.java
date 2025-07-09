package com.example.musica_be.repository.instrumentAnalysis;

import com.example.musica_be.dto.instrumentAnalysis.JobCreateRequestDto;
import com.example.musica_be.dto.instrumentAnalysis.JobCreateResponseDto;
import com.example.musica_be.dto.instrumentAnalysis.UrlResponseDto;

public interface MusicAiClient {
    // GCS(구글 클라우드 스토리지)에 파일 업로드를 위한 Presigned URL 요청
    // 업로드 URL, 다운로드 URL 발급됨 (유효시간 발급 시점으로부터 24시간)
    UrlResponseDto getUrl();
    // 업로드된 파일에 대해 악기 분석 작업(Job) 생성 요청
    JobCreateResponseDto createJob(JobCreateRequestDto request);
}
