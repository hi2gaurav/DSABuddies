package com.dsabuddies.app.dto;

import java.time.LocalDate;

public record EngagementTrendDto(
    LocalDate date,
    long activeUsers,
    long completionsCount
) {}
