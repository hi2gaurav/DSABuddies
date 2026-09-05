package com.dsabuddies.app.dto;

public record MockQuestionDto(
    Long id,
    Long taskId,
    String title,
    String description,
    String link,
    String difficulty,
    String topicName,
    int questionOrder,
    Integer timeSpentSeconds,
    boolean answered,
    Integer selfRating,
    String userNotes
) {}
