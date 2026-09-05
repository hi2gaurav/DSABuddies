package com.dsabuddies.app.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateTaskSheetRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    @NotNull(message = "End date is required")
    LocalDate endDate,

    @NotBlank(message = "Sheet type is required")
    @Pattern(regexp = "^(DAILY|WEEKLY)$", message = "Sheet type must be DAILY or WEEKLY")
    String sheetType
) {}
