package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.LeaderboardEntryDto;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {
    private final TaskCompletionRepository taskCompletionRepository;

    public List<LeaderboardEntryDto> getLeaderboard() {
        List<LeaderboardEntryDto> entries = taskCompletionRepository.findTopUsersByXp();
        List<LeaderboardEntryDto> rankedEntries = new ArrayList<>();
        
        int rank = 1;
        for (LeaderboardEntryDto entry : entries) {
            rankedEntries.add(entry.withRank(rank++));
        }
        
        return rankedEntries;
    }
    
    public List<LeaderboardEntryDto> getWeeklyLeaderboard() {
        // In a real app, this would filter completions by the current week
        // For simplicity, returning the same leaderboard for now
        return getLeaderboard();
    }
}
