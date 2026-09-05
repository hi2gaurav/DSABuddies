package com.dsabuddies.app.dto;

import java.time.LocalDate;
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
    LocalDateTime createdAt,
    int level,
    String title,
    int dailyGoal,
    double consistencyScore,
    boolean streakFreezeAvailable,
    LocalDate streakFreezeUsedDate
) {
    public UserDto(
        Long id,
        String email,
        String name,
        String avatarUrl,
        String role,
        int currentStreak,
        int maxStreak,
        int totalXp,
        LocalDateTime createdAt
    ) {
        this(id, email, name, avatarUrl, role, currentStreak, maxStreak, totalXp, createdAt, 1, "Novice", 3, 0.0, false, null);
    }
}
