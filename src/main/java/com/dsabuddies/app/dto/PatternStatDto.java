package com.dsabuddies.app.dto;

public record PatternStatDto(
    String pattern,
    int totalCount,
    int solvedCount,
    double masteryPercentage
) {}
