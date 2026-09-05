package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.*;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskCompletion;
import com.dsabuddies.app.model.TaskSheet;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.TaskSheetRepository;
import com.dsabuddies.app.repository.TopicRepository;
import com.dsabuddies.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskSheetRepository taskSheetRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final TopicRepository topicRepository;

    public AdminOverviewStatsDto getOverviewStats() {
        List<User> users = userRepository.findAll();
        long totalUsers = users.size();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        long dau = users.stream()
                .filter(u -> today.equals(u.getLastActiveDate()))
                .count();

        long wau = users.stream()
                .filter(u -> u.getLastActiveDate() != null && !u.getLastActiveDate().isBefore(sevenDaysAgo))
                .count();

        long totalCompletions = taskCompletionRepository.count();

        double avgXp = users.isEmpty() ? 0.0 :
                users.stream().mapToInt(User::getTotalXp).average().orElse(0.0);

        long totalSheets = taskSheetRepository.count();
        long totalTasks = taskRepository.count();

        long activeStreaksCount = users.stream()
                .filter(u -> u.getCurrentStreak() > 0)
                .count();

        double avgConsistencyScore = users.isEmpty() ? 0.0 :
                users.stream().mapToDouble(User::getConsistencyScore).average().orElse(0.0);

        return new AdminOverviewStatsDto(
                totalUsers,
                dau,
                wau,
                totalCompletions,
                Math.round(avgXp * 10.0) / 10.0,
                totalSheets,
                totalTasks,
                activeStreaksCount,
                Math.round(avgConsistencyScore * 10.0) / 10.0
        );
    }

    public List<EngagementTrendDto> getEngagementTrend(int days) {
        int targetDays = (days <= 0 || days > 90) ? 14 : days;
        LocalDate startDate = LocalDate.now().minusDays(targetDays - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        List<TaskCompletion> completions = taskCompletionRepository.findByCompletedAtGreaterThanEqual(startDateTime);
        Map<LocalDate, List<TaskCompletion>> completionsByDate = completions.stream()
                .filter(c -> c.getCompletedAt() != null)
                .collect(Collectors.groupingBy(c -> c.getCompletedAt().toLocalDate()));

        List<User> users = userRepository.findAll();
        Map<LocalDate, Long> activeUsersByDate = users.stream()
                .filter(u -> u.getLastActiveDate() != null && !u.getLastActiveDate().isBefore(startDate))
                .collect(Collectors.groupingBy(User::getLastActiveDate, Collectors.counting()));

        List<EngagementTrendDto> trends = new ArrayList<>();
        LocalDate cur = startDate;
        LocalDate today = LocalDate.now();

        while (!cur.isAfter(today)) {
            List<TaskCompletion> dayCompletions = completionsByDate.getOrDefault(cur, Collections.emptyList());
            long completionsCount = dayCompletions.size();
            long uniqueCompletingUsers = dayCompletions.stream()
                    .map(c -> c.getUser().getId())
                    .distinct()
                    .count();

            long activeUsers = Math.max(uniqueCompletingUsers, activeUsersByDate.getOrDefault(cur, 0L));
            trends.add(new EngagementTrendDto(cur, activeUsers, completionsCount));
            cur = cur.plusDays(1);
        }

        return trends;
    }

    public List<TopicDropOffDto> getTopicDropOff() {
        List<Topic> topics = topicRepository.findAll();
        List<Task> allTasks = taskRepository.findAll();
        List<TaskCompletion> allCompletions = taskCompletionRepository.findAll();
        long totalUsers = Math.max(1, userRepository.count());

        Map<Long, List<Task>> tasksByTopic = allTasks.stream()
                .filter(t -> t.getTopic() != null)
                .collect(Collectors.groupingBy(t -> t.getTopic().getId()));

        Map<Long, List<TaskCompletion>> completionsByTopic = allCompletions.stream()
                .filter(c -> c.getTask() != null && c.getTask().getTopic() != null)
                .collect(Collectors.groupingBy(c -> c.getTask().getTopic().getId()));

        List<TopicDropOffDto> results = new ArrayList<>();

        for (Topic topic : topics) {
            List<Task> topicTasks = tasksByTopic.getOrDefault(topic.getId(), Collections.emptyList());
            List<TaskCompletion> topicCompletions = completionsByTopic.getOrDefault(topic.getId(), Collections.emptyList());

            long taskCount = topicTasks.size();
            long compCount = topicCompletions.size();

            double completionRate = 0.0;
            if (taskCount > 0) {
                long totalPossible = taskCount * totalUsers;
                completionRate = Math.min(100.0, ((double) compCount / totalPossible) * 100.0);
            }

            double dropOffRate = Math.max(0.0, 100.0 - completionRate);

            double avgConfidence = topicCompletions.stream()
                    .filter(c -> c.getSelfRating() != null && c.getSelfRating() > 0)
                    .mapToInt(TaskCompletion::getSelfRating)
                    .average()
                    .orElse(0.0);

            results.add(new TopicDropOffDto(
                    topic.getId(),
                    topic.getName(),
                    topic.getColor(),
                    taskCount,
                    compCount,
                    Math.round(completionRate * 10.0) / 10.0,
                    Math.round(dropOffRate * 10.0) / 10.0,
                    Math.round(avgConfidence * 10.0) / 10.0
            ));
        }

        // Sort by dropOffRate descending (most difficult/dropped first)
        results.sort(Comparator.comparingDouble(TopicDropOffDto::dropOffRate).reversed());
        return results;
    }

    public SheetAnalyticsDto getSheetAnalytics(Long sheetId) {
        TaskSheet sheet = taskSheetRepository.findById(sheetId)
                .orElseThrow(() -> new NoSuchElementException("Task sheet not found with id " + sheetId));

        List<Task> tasks = taskRepository.findByTaskSheetId(sheetId);
        long totalUsers = Math.max(1, userRepository.count());

        List<SheetQuestionStatDto> questionStats = new ArrayList<>();
        Set<Long> uniqueUserIds = new HashSet<>();
        double totalRateSum = 0.0;

        for (Task task : tasks) {
            List<TaskCompletion> completions = taskCompletionRepository.findByTaskId(task.getId());
            long count = completions.size();

            for (TaskCompletion c : completions) {
                if (c.getUser() != null) {
                    uniqueUserIds.add(c.getUser().getId());
                }
            }

            double compRate = Math.min(100.0, ((double) count / totalUsers) * 100.0);
            totalRateSum += compRate;

            Integer avgTime = null;
            OptionalDouble avgTimeOpt = completions.stream()
                    .filter(c -> c.getTimeSpentSeconds() != null && c.getTimeSpentSeconds() > 0)
                    .mapToInt(TaskCompletion::getTimeSpentSeconds)
                    .average();
            if (avgTimeOpt.isPresent()) {
                avgTime = (int) Math.round(avgTimeOpt.getAsDouble());
            }

            Double avgRating = null;
            OptionalDouble avgRatingOpt = completions.stream()
                    .filter(c -> c.getSelfRating() != null && c.getSelfRating() > 0)
                    .mapToInt(TaskCompletion::getSelfRating)
                    .average();
            if (avgRatingOpt.isPresent()) {
                avgRating = Math.round(avgRatingOpt.getAsDouble() * 10.0) / 10.0;
            }

            questionStats.add(new SheetQuestionStatDto(
                    task.getId(),
                    task.getTitle(),
                    task.getDifficulty() != null ? task.getDifficulty() : "MEDIUM",
                    task.getTopic() != null ? task.getTopic().getName() : "General",
                    task.getXpReward(),
                    count,
                    Math.round(compRate * 10.0) / 10.0,
                    avgTime,
                    avgRating
            ));
        }

        double overallCompletionRate = tasks.isEmpty() ? 0.0 :
                Math.round((totalRateSum / tasks.size()) * 10.0) / 10.0;

        return new SheetAnalyticsDto(
                sheet.getId(),
                sheet.getTitle(),
                sheet.getSheetType() != null ? sheet.getSheetType() : "DAILY",
                tasks.size(),
                uniqueUserIds.size(),
                overallCompletionRate,
                questionStats
        );
    }
}
