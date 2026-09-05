package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record BookmarkDto(
    Long id,
    Long taskId,
    String taskTitle,
    String taskDescription,
    String difficulty,
    String topicName,
    String topicColor,
    String platformLink,
    int xpReward,
    LocalDateTime createdAt
) {}
