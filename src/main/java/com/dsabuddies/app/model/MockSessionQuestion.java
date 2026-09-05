package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mock_session_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockSessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private MockSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.SET_NULL)
    private Task task;

    private String customTitle;

    @Column(columnDefinition = "TEXT")
    private String customDescription;

    private String customLink;

    private String difficulty;

    private String topicName;

    private int questionOrder;

    private Integer timeSpentSeconds;

    @Builder.Default
    private boolean answered = false;

    private Integer selfRating; // 1-5 confidence rating

    @Column(columnDefinition = "TEXT")
    private String userNotes;
}
