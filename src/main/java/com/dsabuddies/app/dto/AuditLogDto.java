package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record AuditLogDto(
    Long id,
    String adminEmail,
    String adminName,
    String action,
    String entityType,
    String entityId,
    String details,
    String ipAddress,
    LocalDateTime createdAt
) {}
