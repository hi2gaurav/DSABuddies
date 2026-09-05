package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    private String avatarUrl;

    @Builder.Default
    private String role = "ROLE_USER";

    @Builder.Default
    private int currentStreak = 0;

    @Builder.Default
    private int maxStreak = 0;

    private LocalDate lastActiveDate;

    @Builder.Default
    private int totalXp = 0;

    @Builder.Default
    private int level = 1;

    @Builder.Default
    private String title = "Novice";

    @Builder.Default
    private int dailyGoal = 3;

    @Builder.Default
    private double consistencyScore = 0.0;

    @Builder.Default
    private boolean streakFreezeAvailable = false;

    private LocalDate streakFreezeUsedDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
