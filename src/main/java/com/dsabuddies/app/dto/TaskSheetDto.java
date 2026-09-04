package com.dsabuddies.app.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TaskSheetDto(
    Long id,
    String title,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    String sheetType,
    String createdByName,
    List<TaskDto> tasks,
    LocalDateTime createdAt
) {}
