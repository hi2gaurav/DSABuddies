package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_flashcard_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "flashcard_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFlashcardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Builder.Default
    private double easeFactor = 2.5;

    @Builder.Default
    private int intervalDays = 1;

    @Builder.Default
    private LocalDate nextReviewDate = LocalDate.now();

    @Builder.Default
    private int reviewCount = 0;

    private LocalDateTime lastReviewedAt;
}
