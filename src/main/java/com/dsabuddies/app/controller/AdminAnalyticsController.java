package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.*;
import com.dsabuddies.app.service.AdminAnalyticsService;
import com.dsabuddies.app.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;
    private final AuditService auditService;

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewStatsDto> getOverviewStats() {
        return ResponseEntity.ok(adminAnalyticsService.getOverviewStats());
    }

    @GetMapping("/engagement")
    public ResponseEntity<List<EngagementTrendDto>> getEngagementTrends(
            @RequestParam(name = "days", defaultValue = "14") int days) {
        return ResponseEntity.ok(adminAnalyticsService.getEngagementTrend(days));
    }

    @GetMapping("/topic-dropoff")
    public ResponseEntity<List<TopicDropOffDto>> getTopicDropOff() {
        return ResponseEntity.ok(adminAnalyticsService.getTopicDropOff());
    }

    @GetMapping("/sheet-stats/{sheetId}")
    public ResponseEntity<SheetAnalyticsDto> getSheetAnalytics(@PathVariable Long sheetId) {
        return ResponseEntity.ok(adminAnalyticsService.getSheetAnalytics(sheetId));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDto>> getAuditLogs() {
        return ResponseEntity.ok(auditService.getRecentLogs());
    }
}
