package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record BadgeDto(
    Long id,
    String name,
    String description,
    String icon,
    String category,
    String criteriaType,
    int criteriaValue,
    int xpReward,
    String rarity,
    boolean earned,
    LocalDateTime earnedAt,
    double progressPercent
) {}
