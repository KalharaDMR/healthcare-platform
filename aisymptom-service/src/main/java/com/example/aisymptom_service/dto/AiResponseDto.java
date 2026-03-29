package com.example.aisymptom_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiResponseDto {
    private List<RecommendedSpecialization> recommendedSpecializations = List.of();

    private String generalAdvice;

    private Double confidenceScore;
}
