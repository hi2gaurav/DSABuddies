package com.dsabuddies.app.dto;

public record AdminOverviewStatsDto(
    long totalUsers,
    long dailyActiveUsers,
    long weeklyActiveUsers,
    long totalCompletions,
    double averageXp,
    long totalTaskSheets,
    long totalTasks,
    long activeStreaksCount,
    double averageConsistencyScore
) {}
