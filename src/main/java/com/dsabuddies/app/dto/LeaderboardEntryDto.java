package com.dsabuddies.app.dto;

public record LeaderboardEntryDto(
    int rank,
    Long userId,
    String userName,
    String userAvatar,
    int totalXp,
    int currentStreak,
    int tasksCompleted,
    int level,
    String title
) {
    public LeaderboardEntryDto(int rank, Long userId, String userName, String userAvatar, int totalXp, int currentStreak, int tasksCompleted) {
        this(rank, userId, userName, userAvatar, totalXp, currentStreak, tasksCompleted, 1, "Novice");
    }

    public LeaderboardEntryDto withRank(int rank) {
        return new LeaderboardEntryDto(rank, userId, userName, userAvatar, totalXp, currentStreak, tasksCompleted, level, title);
    }
}
