package com.dsabuddies.app.dto;

public record WeakTopicDto(
    Long topicId,
    String topicName,
    String topicColor,
    int totalProblems,
    int solvedProblems,
    double completionPercentage,
    Double averageRating,
    String recommendation
) {}
