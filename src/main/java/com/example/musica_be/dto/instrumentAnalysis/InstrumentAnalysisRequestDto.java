package com.example.musica_be.dto.instrumentAnalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InstrumentAnalysisRequestDto {
    private Long lectureId;       // 강의 id (어떤 강의 영상인지 구분)
    private String s3DownloadUrl; // S3에서 발급한 다운로드 가능한 Presigned URL
}
