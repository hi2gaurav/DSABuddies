package com.dsabuddies.app.dto;

public record UpdateSettingsRequest(
    Integer dailyGoal,
    Boolean useStreakFreeze
) {}
