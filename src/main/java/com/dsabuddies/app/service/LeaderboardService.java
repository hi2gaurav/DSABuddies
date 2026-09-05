package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.LeaderboardEntryDto;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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
        LocalDateTime startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        return getLeaderboardSince(startOfWeek);
    }

    public List<LeaderboardEntryDto> getMonthlyLeaderboard() {
        LocalDateTime startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        return getLeaderboardSince(startOfMonth);
    }

    private List<LeaderboardEntryDto> getLeaderboardSince(LocalDateTime since) {
        List<Object[]> rows = taskCompletionRepository.findLeaderboardSince(since);
        List<LeaderboardEntryDto> ranked = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            Long userId = ((Number) row[0]).longValue();
            String userName = (String) row[1];
            String avatarUrl = (String) row[2];
            int currentStreak = ((Number) row[3]).intValue();
            int level = row[4] != null ? ((Number) row[4]).intValue() : 1;
            String title = row[5] != null ? (String) row[5] : "Novice";
            int xpGained = row[6] != null ? ((Number) row[6]).intValue() : 0;
            int tasksCompleted = row[7] != null ? ((Number) row[7]).intValue() : 0;

            ranked.add(new LeaderboardEntryDto(
                    rank++,
                    userId,
                    userName,
                    avatarUrl,
                    xpGained,
                    currentStreak,
                    tasksCompleted,
                    level,
                    title
            ));
        }
        return ranked;
    }
}
