package com.dsabuddies.app.dto;

public record SubmitFlashcardReviewRequest(
    int rating // 1-5 (SM-2 scale: 1=Again, 2=Hard, 3=Good, 4=Easy, 5=Mastered)
) {}
