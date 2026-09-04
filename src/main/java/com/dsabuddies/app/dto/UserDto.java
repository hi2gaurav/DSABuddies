package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record UserDto(
    Long id,
    String email,
    String name,
    String avatarUrl,
    String role,
    int currentStreak,
    int maxStreak,
    int totalXp,
    LocalDateTime createdAt
) {}
