package com.dsabuddies.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_created_at", columnList = "createdAt"),
    @Index(name = "idx_audit_logs_action", columnList = "action"),
    @Index(name = "idx_audit_logs_entity_type", columnList = "entityType")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adminEmail;

    private String adminName;

    @Column(nullable = false)
    private String action;

    private String entityType;

    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String ipAddress;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
