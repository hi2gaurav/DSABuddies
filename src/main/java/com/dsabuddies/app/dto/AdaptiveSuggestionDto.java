package com.dsabuddies.app.dto;

public record AdaptiveSuggestionDto(
    Long taskId,
    String title,
    String difficulty,
    String topicName,
    String topicColor,
    String platformLink,
    int xpReward,
    String reason
) {}
