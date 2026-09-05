package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.AuditLogDto;
import com.dsabuddies.app.model.AuditLog;
import com.dsabuddies.app.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String adminEmail, String adminName, String action, String entityType, String entityId, String details, String ipAddress) {
        AuditLog entry = AuditLog.builder()
                .adminEmail(adminEmail != null ? adminEmail : "system")
                .adminName(adminName != null ? adminName : "Admin")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(entry);
    }

    public void logWithRequest(String adminEmail, String adminName, String action, String entityType, String entityId, String details, HttpServletRequest request) {
        String ip = extractClientIp(request);
        log(adminEmail, adminName, action, entityType, entityId, details, ip);
    }

    public List<AuditLogDto> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AuditLogDto> getLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public String extractClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private AuditLogDto toDto(AuditLog log) {
        return new AuditLogDto(
                log.getId(),
                log.getAdminEmail(),
                log.getAdminName(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}
