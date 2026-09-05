package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.AdaptiveSuggestionDto;
import com.dsabuddies.app.dto.PatternStatDto;
import com.dsabuddies.app.dto.WeakTopicDto;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskCompletion;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeakAreaService {

    private final TopicRepository topicRepository;
    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;

    @Transactional(readOnly = true)
    public List<WeakTopicDto> getWeakTopics(Long userId) {
        List<Topic> allTopics = topicRepository.findAll();
        List<TaskCompletion> completions = taskCompletionRepository.findByUserId(userId);
        Map<Long, List<TaskCompletion>> completionsByTopic = completions.stream()
                .filter(tc -> tc.getTask().getTopic() != null)
                .collect(Collectors.groupingBy(tc -> tc.getTask().getTopic().getId()));

        List<WeakTopicDto> weakTopics = new ArrayList<>();

        for (Topic topic : allTopics) {
            long totalProblems = taskRepository.countByTopicId(topic.getId());
            if (totalProblems == 0) continue;

            List<TaskCompletion> topicCompletions = completionsByTopic.getOrDefault(topic.getId(), Collections.emptyList());
            int solved = topicCompletions.size();
            double completionPct = Math.round(((double) solved / totalProblems) * 1000.0) / 10.0;

            // Calculate average confidence rating if provided
            OptionalDouble avgRatingOpt = topicCompletions.stream()
                    .filter(tc -> tc.getSelfRating() != null)
                    .mapToInt(TaskCompletion::getSelfRating)
                    .average();

            Double avgRating = avgRatingOpt.isPresent() ? Math.round(avgRatingOpt.getAsDouble() * 10.0) / 10.0 : null;

            // Consider weak if completion < 50% or average rating < 3.0 (when rated)
            boolean isWeak = completionPct < 50.0 || (avgRating != null && avgRating < 3.0);

            if (isWeak) {
                String recommendation;
                if (completionPct < 25.0) {
                    recommendation = "Beginner stage in " + topic.getName() + ". Practice 2 Easy problems to build intuition.";
                } else if (avgRating != null && avgRating < 3.0) {
                    recommendation = "Low confidence scores (" + avgRating + "/5). Review key patterns and solve Mediums.";
                } else {
                    recommendation = "Moderate progress (" + completionPct + "%). Push to solve remaining problems.";
                }

                weakTopics.add(new WeakTopicDto(
                        topic.getId(),
                        topic.getName(),
                        topic.getColor(),
                        (int) totalProblems,
                        solved,
                        completionPct,
                        avgRating,
                        recommendation
                ));
            }
        }

        // Sort by completion percentage ascending (weakest first)
        weakTopics.sort(Comparator.comparingDouble(WeakTopicDto::completionPercentage));
        return weakTopics;
    }

    @Transactional(readOnly = true)
    public List<AdaptiveSuggestionDto> getAdaptiveSuggestions(Long userId) {
        List<WeakTopicDto> weakTopics = getWeakTopics(userId);
        if (weakTopics.isEmpty()) {
            // If user has mastered all or hasn't started, grab top 5 general problems
            return Collections.emptyList();
        }

        Set<Long> solvedTaskIds = taskCompletionRepository.findByUserId(userId).stream()
                .map(tc -> tc.getTask().getId())
                .collect(Collectors.toSet());

        List<AdaptiveSuggestionDto> suggestions = new ArrayList<>();

        for (WeakTopicDto wt : weakTopics) {
            List<Task> topicTasks = taskRepository.findByTopicId(wt.topicId());
            for (Task task : topicTasks) {
                if (!solvedTaskIds.contains(task.getId())) {
                    suggestions.add(new AdaptiveSuggestionDto(
                            task.getId(),
                            task.getTitle(),
                            task.getDifficulty(),
                            wt.topicName(),
                            wt.topicColor(),
                            task.getPlatformLink(),
                            task.getXpReward(),
                            "Recommended because your " + wt.topicName() + " mastery is at " + wt.completionPercentage() + "%"
                    ));

                    if (suggestions.size() >= 5) {
                        return suggestions;
                    }
                }
            }
        }

        return suggestions;
    }

    @Transactional(readOnly = true)
    public List<PatternStatDto> getPatternStats(Long userId) {
        List<Task> allTasks = taskRepository.findAll();
        Set<Long> solvedTaskIds = taskCompletionRepository.findByUserId(userId).stream()
                .map(tc -> tc.getTask().getId())
                .collect(Collectors.toSet());

        Map<String, int[]> patternCounts = new HashMap<>(); // [total, solved]

        for (Task task : allTasks) {
            String tags = task.getPatternTags();
            if (tags == null || tags.isBlank()) {
                // Fallback to topic name as base pattern if not specifically tagged
                if (task.getTopic() != null) {
                    tags = task.getTopic().getName();
                } else {
                    continue;
                }
            }

            boolean isSolved = solvedTaskIds.contains(task.getId());
            String[] split = tags.split(",");
            for (String rawPattern : split) {
                String pattern = rawPattern.trim();
                if (pattern.isEmpty()) continue;

                int[] counts = patternCounts.computeIfAbsent(pattern, k -> new int[2]);
                counts[0]++; // total
                if (isSolved) {
                    counts[1]++; // solved
                }
            }
        }

        return patternCounts.entrySet().stream()
                .map(entry -> {
                    int total = entry.getValue()[0];
                    int solved = entry.getValue()[1];
                    double mastery = total > 0 ? Math.round(((double) solved / total) * 1000.0) / 10.0 : 0.0;
                    return new PatternStatDto(entry.getKey(), total, solved, mastery);
                })
                .sorted(Comparator.comparingInt(PatternStatDto::totalCount).reversed())
                .collect(Collectors.toList());
    }
}
