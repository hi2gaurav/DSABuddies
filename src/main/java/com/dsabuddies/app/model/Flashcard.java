package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // "JAVA", "SPRING_BOOT", "DATABASE", "CS_FUNDAMENTALS"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(columnDefinition = "TEXT")
    private String codeSnippet;

    @Builder.Default
    private String difficulty = "MEDIUM"; // "EASY", "MEDIUM", "HARD"

    private String topic; // Sub-topic e.g. "Multithreading", "Transactions", "Indexes"
}
