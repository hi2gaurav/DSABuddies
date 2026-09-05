package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
}
