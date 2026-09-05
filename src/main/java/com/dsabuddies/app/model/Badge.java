package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String icon; // Icon key e.g. "Flame", "Zap", "Award", "Shield", "Trophy", "Target"

    private String category; // "STREAK", "PROBLEMS", "XP", "SPECIAL"

    private String criteriaType; // "STREAK_DAYS", "PROBLEMS_SOLVED", "XP_EARNED", "SPEED_SOLVE", "NIGHT_OWL", "CONSISTENCY"

    private int criteriaValue; // Threshold value (e.g. 7 for 7-day streak)

    private int xpReward; // Bonus XP granted when unlocked

    private String rarity; // "COMMON", "RARE", "EPIC", "LEGENDARY"
}
