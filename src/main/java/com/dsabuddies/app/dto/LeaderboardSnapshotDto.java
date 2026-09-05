package com.dsabuddies.app.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaderboardSnapshotDto(
    Long id,
    String periodType,
    LocalDate periodStart,
    LocalDate periodEnd,
    String snapshotData,
    LocalDateTime createdAt
) {}
