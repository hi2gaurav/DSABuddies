package com.dsabuddies.app.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReviewItemDto(
    Long id,
    Long taskId,
    String taskTitle,
    String taskDescription,
    String difficulty,
    String topicName,
    String topicColor,
    String platformLink,
    int xpReward,
    LocalDate nextReviewDate,
    int intervalDays,
    double easeFactor,
    int reviewCount,
    LocalDateTime lastReviewedAt
) {}
