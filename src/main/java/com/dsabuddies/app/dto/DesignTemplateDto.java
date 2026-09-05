package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record DesignTemplateDto(
    Long id,
    String title,
    String category,
    String overview,
    String requirements,
    String components,
    String diagramData,
    String sampleSolution,
    String difficulty,
    String tags,
    LocalDateTime createdAt
) {}
