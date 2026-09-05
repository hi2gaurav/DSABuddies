package com.dsabuddies.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MockSessionDto(
    Long id,
    Long userId,
    String mode,
    String difficultyFilter,
    String topicFilter,
    int questionCount,
    int timeLimitMinutes,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    int score,
    int xpAwarded,
    String status,
    List<MockQuestionDto> questions
) {}
