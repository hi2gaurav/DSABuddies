package com.dsabuddies.app.dto;

public record TopicDropOffDto(
    Long topicId,
    String topicName,
    String topicColor,
    long totalTasks,
    long totalCompletions,
    double completionRate,
    double dropOffRate,
    double avgConfidence
) {}
