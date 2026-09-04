package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record TaskCompletionDto(
    Long id,
    Long userId,
    String userName,
    String userAvatar,
    Long taskId,
    String taskTitle,
    LocalDateTime completedAt,
    String solutionLink,
    String notes
) {}
