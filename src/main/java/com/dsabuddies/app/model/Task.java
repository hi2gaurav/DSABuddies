package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private String difficulty; // 'EASY', 'MEDIUM', 'HARD'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    private String platformLink;

    private int xpReward;

    private String companyTags; // e.g. "Amazon,Google,Flipkart"
    private String patternTags; // e.g. "Two Pointer,Sliding Window,DP"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_sheet_id")
    private TaskSheet taskSheet;
}
