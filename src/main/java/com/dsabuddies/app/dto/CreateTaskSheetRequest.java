package com.dsabuddies.app.dto;

import java.time.LocalDate;

public record CreateTaskSheetRequest(
    String title,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    String sheetType
) {}
