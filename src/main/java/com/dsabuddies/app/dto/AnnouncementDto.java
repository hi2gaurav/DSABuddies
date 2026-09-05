package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record AnnouncementDto(
    Long id,
    String title,
    String message,
    String priority,
    boolean active,
    String authorName,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {}
