package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.LeaderboardEntryDto;
import com.dsabuddies.app.service.LeaderboardService;
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

    @GetMapping
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(@RequestParam(required = false) String period) {
        if ("weekly".equals(period)) {
            return ResponseEntity.ok(leaderboardService.getWeeklyLeaderboard());
        }
        return ResponseEntity.ok(leaderboardService.getLeaderboard());
    }
}
