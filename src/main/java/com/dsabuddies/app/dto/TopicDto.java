package com.dsabuddies.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TopicDto(
    Long id,
    @NotBlank(message = "Topic name is required")
    @Size(max = 100, message = "Topic name must not exceed 100 characters")
    String name,

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex code (e.g. #3B82F6)")
    String color,

    @Size(max = 10, message = "Icon must be a short emoji or code")
    String icon
) {}
