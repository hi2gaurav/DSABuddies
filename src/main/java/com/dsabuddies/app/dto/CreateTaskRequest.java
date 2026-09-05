package com.dsabuddies.app.dto;

import jakarta.validation.constraints.*;

public record CreateTaskRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    @NotBlank(message = "Difficulty is required")
    @Pattern(regexp = "^(EASY|MEDIUM|HARD)$", message = "Difficulty must be EASY, MEDIUM, or HARD")
    String difficulty,

    @NotNull(message = "Topic ID is required")
    Long topicId,

    @Size(max = 500, message = "Platform link must not exceed 500 characters")
    String platformLink,

    @Min(value = 0, message = "XP reward must be non-negative")
    @Max(value = 10000, message = "XP reward must not exceed 10000")
    int xpReward,

    @NotNull(message = "Task sheet ID is required")
    Long taskSheetId
) {}
