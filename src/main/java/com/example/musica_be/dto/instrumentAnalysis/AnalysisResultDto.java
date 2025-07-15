package com.example.musica_be.dto.instrumentAnalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AnalysisResultDto {
    private List<String> detectedInstruments;
}