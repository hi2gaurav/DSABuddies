package com.dsabuddies.app.dto;

import jakarta.validation.constraints.NotBlank;

public record BroadcastRequest(
    @NotBlank String title,
    @NotBlank String message,
    String priority, // NORMAL, HIGH, URGENT
    Integer expiresInDays
) {}
