package com.dsabuddies.app.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteTaskRequest(
    @Pattern(regexp = "^(https?://.*)?$", message = "Solution link must be a valid http or https URL")
    @Size(max = 500, message = "Solution link must not exceed 500 characters")
    String solutionLink,

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    String notes,

    Integer timeSpentSeconds,

    @jakarta.validation.constraints.Min(value = 1, message = "Self rating must be at least 1")
    @jakarta.validation.constraints.Max(value = 5, message = "Self rating cannot exceed 5")
    Integer selfRating
) {}
