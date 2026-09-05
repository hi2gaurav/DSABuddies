package com.dsabuddies.app.dto;

public record SheetQuestionStatDto(
    Long taskId,
    String taskTitle,
    String difficulty,
    String topicName,
    int xpReward,
    long completionsCount,
    double completionRate,
    Integer avgTimeSpentSeconds,
    Double avgSelfRating
) {}
