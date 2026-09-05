package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_queues", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "task_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private LocalDate nextReviewDate;

    @Builder.Default
    private int intervalDays = 1;

    @Builder.Default
    private double easeFactor = 2.5; // SM-2 standard default

    @Builder.Default
    private int reviewCount = 0;

    private LocalDateTime lastReviewedAt;

    @PrePersist
    protected void onCreate() {
        if (nextReviewDate == null) {
            nextReviewDate = LocalDate.now().plusDays(1);
        }
    }
}
