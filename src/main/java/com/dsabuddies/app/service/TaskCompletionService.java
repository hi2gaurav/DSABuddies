package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.CompleteTaskRequest;
import com.dsabuddies.app.dto.TaskCompletionDto;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskCompletion;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskCompletionService {
    private final TaskCompletionRepository taskCompletionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public TaskCompletionDto completeTask(Long taskId, Long userId, CompleteTaskRequest request) {
        if (taskCompletionRepository.existsByUserIdAndTaskId(userId, taskId)) {
            throw new RuntimeException("Task already completed");
        }
        
        User user = userRepository.findById(userId).orElseThrow();
        Task task = taskRepository.findById(taskId).orElseThrow();
        
        TaskCompletion completion = TaskCompletion.builder()
                .user(user)
                .task(task)
                .solutionLink(request != null ? request.solutionLink() : null)
                .notes(request != null ? request.notes() : null)
                .completedAt(LocalDateTime.now())
                .build();
                
        completion = taskCompletionRepository.save(completion);
        
        user.setTotalXp(user.getTotalXp() + task.getXpReward());
        userService.updateStreak(user); // Also saves user
        
        return toDto(completion);
    }
    
    @Transactional
    public void uncompleteTask(Long taskId, Long userId) {
        TaskCompletion completion = taskCompletionRepository.findByUserIdAndTaskId(userId, taskId)
                .orElseThrow(() -> new RuntimeException("Completion not found"));
                
        User user = completion.getUser();
        user.setTotalXp(Math.max(0, user.getTotalXp() - completion.getTask().getXpReward()));
        userRepository.save(user);
        
        taskCompletionRepository.delete(completion);
    }
    
    public List<TaskCompletionDto> getUserCompletions(Long userId) {
        return taskCompletionRepository.findByUserId(userId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }
    
    private TaskCompletionDto toDto(TaskCompletion c) {
        return new TaskCompletionDto(
                c.getId(), c.getUser().getId(), c.getUser().getName(), c.getUser().getAvatarUrl(),
                c.getTask().getId(), c.getTask().getTitle(), c.getCompletedAt(),
                c.getSolutionLink(), c.getNotes()
        );
    }
}
