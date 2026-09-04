package com.dsabuddies.app.dto;

public record TaskDto(
    Long id,
    String title,
    String description,
    String difficulty,
    String topicName,
    String topicColor,
    String platformLink,
    int xpReward,
    boolean completed
) {}
