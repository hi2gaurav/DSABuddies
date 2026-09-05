package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.LeaderboardEntryDto;
import com.dsabuddies.app.dto.LeaderboardSnapshotDto;
import com.dsabuddies.app.model.LeaderboardSnapshot;
import com.dsabuddies.app.repository.LeaderboardSnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardSnapshotService {

    private final LeaderboardSnapshotRepository snapshotRepository;
    private final LeaderboardService leaderboardService;
    private final ObjectMapper objectMapper;

    @Transactional
    public LeaderboardSnapshotDto createSnapshot(String periodType) {
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end = today.minusDays(1);

        if ("WEEKLY".equalsIgnoreCase(periodType)) {
            start = today.minusDays(7);
        } else {
            start = today.minusMonths(1);
        }

        List<LeaderboardEntryDto> entries = "WEEKLY".equalsIgnoreCase(periodType)
                ? leaderboardService.getWeeklyLeaderboard()
                : leaderboardService.getMonthlyLeaderboard();

        String jsonData = "[]";
        try {
            jsonData = objectMapper.writeValueAsString(entries.stream().limit(10).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Failed to serialize leaderboard snapshot data", e);
        }

        LeaderboardSnapshot snapshot = LeaderboardSnapshot.builder()
                .periodType(periodType.toUpperCase())
                .periodStart(start)
                .periodEnd(end)
                .snapshotData(jsonData)
                .createdAt(LocalDateTime.now())
                .build();

        snapshot = snapshotRepository.save(snapshot);
        return toDto(snapshot);
    }

    @Scheduled(cron = "0 0 0 * * MON") // Every Monday midnight
    public void archiveWeeklyLeaderboard() {
        createSnapshot("WEEKLY");
    }

    @Scheduled(cron = "0 0 0 1 * *") // 1st of every month
    public void archiveMonthlyLeaderboard() {
        createSnapshot("MONTHLY");
    }

    public List<LeaderboardSnapshotDto> getSnapshots(String periodType) {
        return snapshotRepository.findByPeriodTypeOrderByCreatedAtDesc(periodType.toUpperCase())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private LeaderboardSnapshotDto toDto(LeaderboardSnapshot s) {
        return new LeaderboardSnapshotDto(
                s.getId(),
                s.getPeriodType(),
                s.getPeriodStart(),
                s.getPeriodEnd(),
                s.getSnapshotData(),
                s.getCreatedAt()
        );
    }
}
