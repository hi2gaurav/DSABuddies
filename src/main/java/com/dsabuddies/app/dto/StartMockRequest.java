package com.dsabuddies.app.dto;

public record StartMockRequest(
    String mode, // "DSA", "BEHAVIORAL", "SYSTEM_DESIGN"
    String difficultyFilter, // "EASY", "MEDIUM", "HARD", "MIXED"
    String topicFilter, // optional topic name or tag
    Integer questionCount, // default 2
    Integer timeLimitMinutes // default 45
) {}
