package com.dsabuddies.app.dto;

import jakarta.validation.constraints.Size;

public record SaveNoteRequest(
    @Size(max = 10000, message = "Note content must not exceed 10000 characters")
    String content,

    @Size(max = 20000, message = "Code snippet must not exceed 20000 characters")
    String codeSnippet,

    @Size(max = 50, message = "Language must not exceed 50 characters")
    String language
) {}
