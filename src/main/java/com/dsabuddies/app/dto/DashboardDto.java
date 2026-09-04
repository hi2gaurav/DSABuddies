package com.dsabuddies.app.dto;

import java.util.List;

public record DashboardDto(
    String userName,
    String avatarUrl,
    int currentStreak,
    int maxStreak,
    int totalXp,
    int tasksCompleted,
    int totalTasks,
    double completionPercentage,
    TaskSheetDto activeSheet,
    List<TaskCompletionDto> recentCompletions
) {}
