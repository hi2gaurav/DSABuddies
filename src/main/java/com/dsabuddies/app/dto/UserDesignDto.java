package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record UserDesignDto(
    Long id,
    Long userId,
    Long templateId,
    String templateTitle,
    String title,
    String content,
    String diagramData,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
