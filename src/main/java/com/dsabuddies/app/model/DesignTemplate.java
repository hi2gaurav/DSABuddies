package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "design_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category; // "HLD" or "LLD"

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(columnDefinition = "TEXT")
    private String requirements; // Functional and Non-functional

    @Column(columnDefinition = "TEXT")
    private String components; // Core components checklist

    @Column(columnDefinition = "TEXT")
    private String diagramData; // Architecture diagram/Mermaid structure

    @Column(columnDefinition = "TEXT")
    private String sampleSolution; // Comprehensive reference design

    @Builder.Default
    private String difficulty = "INTERMEDIATE"; // "BEGINNER", "INTERMEDIATE", "ADVANCED"

    private String tags; // e.g. "Caching, Database, WebSocket" or "OOP, Design Patterns"

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
