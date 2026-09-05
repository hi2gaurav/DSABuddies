package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.AuditLogDto;
import com.dsabuddies.app.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuditServiceTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("AuditService should correctly persist and retrieve audit logs")
    void testAuditLogCreation() {
        auditService.log(
                "admin@dsabuddies.com",
                "Admin User",
                "CREATE_SHEET",
                "TASK_SHEET",
                "101",
                "Created sheet Week 1",
                "192.168.1.1"
        );

        List<AuditLogDto> logs = auditService.getRecentLogs();
        assertThat(logs).isNotEmpty();

        AuditLogDto latest = logs.get(0);
        assertThat(latest.adminEmail()).isEqualTo("admin@dsabuddies.com");
        assertThat(latest.action()).isEqualTo("CREATE_SHEET");
        assertThat(latest.entityType()).isEqualTo("TASK_SHEET");
        assertThat(latest.entityId()).isEqualTo("101");
        assertThat(latest.details()).contains("Week 1");
    }
}
