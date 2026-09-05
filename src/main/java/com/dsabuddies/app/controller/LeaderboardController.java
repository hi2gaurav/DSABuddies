package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.LeaderboardEntryDto;
import com.dsabuddies.app.dto.LeaderboardSnapshotDto;
import com.dsabuddies.app.service.LeaderboardService;
import com.dsabuddies.app.service.LeaderboardSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final LeaderboardSnapshotService snapshotService;

    @GetMapping
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(@RequestParam(required = false) String period) {
        if ("weekly".equalsIgnoreCase(period)) {
            return ResponseEntity.ok(leaderboardService.getWeeklyLeaderboard());
        } else if ("monthly".equalsIgnoreCase(period)) {
            return ResponseEntity.ok(leaderboardService.getMonthlyLeaderboard());
        }
        return ResponseEntity.ok(leaderboardService.getLeaderboard());
    }

    @GetMapping("/history")
    public ResponseEntity<List<LeaderboardSnapshotDto>> getHistory(@RequestParam(defaultValue = "weekly") String period) {
        return ResponseEntity.ok(snapshotService.getSnapshots(period));
    }
}
