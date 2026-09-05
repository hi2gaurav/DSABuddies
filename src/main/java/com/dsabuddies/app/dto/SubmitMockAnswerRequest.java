package com.dsabuddies.app.dto;

public record SubmitMockAnswerRequest(
    Integer timeSpentSeconds,
    Integer selfRating, // 1-5 confidence rating
    String userNotes,
    Boolean answered
) {}
