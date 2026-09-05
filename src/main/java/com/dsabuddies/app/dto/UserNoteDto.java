package com.dsabuddies.app.dto;

import java.time.LocalDateTime;

public record UserNoteDto(
    Long id,
    Long taskId,
    String taskTitle,
    String content,
    String codeSnippet,
    String language,
    LocalDateTime updatedAt
) {}
