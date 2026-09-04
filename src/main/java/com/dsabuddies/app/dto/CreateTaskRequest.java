package com.dsabuddies.app.dto;

public record CreateTaskRequest(
    String title,
    String description,
    String difficulty,
    Long topicId,
    String platformLink,
    int xpReward,
    Long taskSheetId
) {}
