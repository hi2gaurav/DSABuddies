package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private String mode = "DSA"; // "DSA", "BEHAVIORAL", "SYSTEM_DESIGN"

    @Builder.Default
    private String difficultyFilter = "MIXED"; // "EASY", "MEDIUM", "HARD", "MIXED"

    private String topicFilter; // optional topic name or ID

    @Builder.Default
    private int questionCount = 2;

    @Builder.Default
    private int timeLimitMinutes = 45;

    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    @Builder.Default
    private int score = 0; // Number of questions solved/satisfactory

    @Builder.Default
    private int xpAwarded = 0;

    @Builder.Default
    private String status = "IN_PROGRESS"; // "IN_PROGRESS", "COMPLETED", "ABANDONED"

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    @Builder.Default
    private List<MockSessionQuestion> questions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}
