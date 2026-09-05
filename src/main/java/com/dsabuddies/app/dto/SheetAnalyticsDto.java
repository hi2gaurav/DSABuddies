package com.dsabuddies.app.dto;

import java.util.List;

public record SheetAnalyticsDto(
    Long sheetId,
    String sheetTitle,
    String sheetType,
    int totalTasks,
    long uniqueCompletedUsers,
    double overallCompletionRate,
    List<SheetQuestionStatDto> questionStats
) {}
