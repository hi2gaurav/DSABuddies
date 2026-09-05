package com.dsabuddies.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TaskCompletionDto(
    Long id,
    Long userId,
    String userName,
    String userAvatar,
    Long taskId,
    String taskTitle,
    LocalDateTime completedAt,
    String solutionLink,
    String notes,
    List<BadgeDto> newBadges,
    boolean levelUp,
    int newLevel,
    String newTitle
) {
    public TaskCompletionDto(
        Long id,
        Long userId,
        String userName,
        String userAvatar,
        Long taskId,
        String taskTitle,
        LocalDateTime completedAt,
        String solutionLink,
        String notes
    ) {
        this(id, userId, userName, userAvatar, taskId, taskTitle, completedAt, solutionLink, notes, List.of(), false, 1, "Novice");
    }
}
