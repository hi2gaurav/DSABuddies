package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.DashboardDto;
import com.dsabuddies.app.dto.TaskCompletionDto;
import com.dsabuddies.app.dto.TaskSheetDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TaskCompletionRepository taskCompletionRepository;
    private final TaskRepository taskRepository;
    private final TaskSheetService taskSheetService;

    public DashboardDto getDashboard(User user) {
        long tasksCompleted = taskCompletionRepository.countByUserId(user.getId());
        long totalTasks = taskRepository.count();
        double completionPercentage = totalTasks > 0 ? (double) tasksCompleted / totalTasks * 100 : 0;
        
        List<TaskSheetDto> activeSheets = taskSheetService.getActiveTaskSheets(user.getId());
        TaskSheetDto activeSheet = activeSheets.isEmpty() ? null : activeSheets.get(0);
        
        List<TaskCompletionDto> recentCompletions = taskCompletionRepository.findByUserId(user.getId())
                .stream()
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
                .limit(5)
                .map(c -> new TaskCompletionDto(
                        c.getId(), c.getUser().getId(), c.getUser().getName(), c.getUser().getAvatarUrl(),
                        c.getTask().getId(), c.getTask().getTitle(), c.getCompletedAt(),
                        c.getSolutionLink(), c.getNotes()
                ))
                .collect(Collectors.toList());

        return new DashboardDto(
                user.getName(),
                user.getAvatarUrl(),
                user.getCurrentStreak(),
                user.getMaxStreak(),
                user.getTotalXp(),
                (int) tasksCompleted,
                (int) totalTasks,
                completionPercentage,
                activeSheet,
                recentCompletions
        );
    }
}
