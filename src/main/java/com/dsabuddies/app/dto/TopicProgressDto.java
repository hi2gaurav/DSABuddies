package com.dsabuddies.app.dto;

public record TopicProgressDto(
    String topicName,
    String topicColor,
    int completed,
    int total,
    double percentage
) {}
