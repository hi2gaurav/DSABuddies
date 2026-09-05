package com.dsabuddies.app.dto;

import java.time.LocalDate;

public record FlashcardDto(
    Long id,
    String category,
    String question,
    String answer,
    String codeSnippet,
    String difficulty,
    String topic,
    Double easeFactor,
    Integer intervalDays,
    LocalDate nextReviewDate,
    Integer reviewCount,
    boolean due
) {}
