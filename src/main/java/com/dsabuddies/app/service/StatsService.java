package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.TopicProgressDto;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskCompletion;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final TopicRepository topicRepository;
    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;

    public List<TopicProgressDto> getTopicProgress(Long userId) {
        List<Topic> topics = topicRepository.findAll();
        List<TaskCompletion> userCompletions = taskCompletionRepository.findByUserId(userId);
        
        List<Long> completedTaskIds = userCompletions.stream()
                .map(tc -> tc.getTask().getId())
                .collect(Collectors.toList());
                
        List<TopicProgressDto> result = new ArrayList<>();
        
        for (Topic topic : topics) {
            List<Task> topicTasks = taskRepository.findByTopicId(topic.getId());
            int total = topicTasks.size();
            
            if (total > 0) {
                int completed = (int) topicTasks.stream()
                        .filter(t -> completedTaskIds.contains(t.getId()))
                        .count();
                
                result.add(new TopicProgressDto(
                        topic.getName(),
                        topic.getColor(),
                        completed,
                        total,
                        (double) completed / total * 100
                ));
            }
        }
        
        return result;
    }
    
    public Map<String, Integer> getActivityData(Long userId, int months) {
        List<TaskCompletion> completions = taskCompletionRepository.findByUserId(userId);
        Map<String, Integer> activityMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (TaskCompletion completion : completions) {
            String dateStr = completion.getCompletedAt().format(formatter);
            activityMap.put(dateStr, activityMap.getOrDefault(dateStr, 0) + 1);
        }
        
        return activityMap;
    }
}
