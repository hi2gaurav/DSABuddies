package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.CreateTaskRequest;
import com.dsabuddies.app.dto.TaskDto;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.TaskSheetRepository;
import com.dsabuddies.app.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskSheetRepository taskSheetRepository;
    private final TopicRepository topicRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final com.dsabuddies.app.repository.ReviewQueueRepository reviewQueueRepository;
    private final com.dsabuddies.app.repository.BookmarkRepository bookmarkRepository;
    private final com.dsabuddies.app.repository.UserNoteRepository userNoteRepository;
    private final com.dsabuddies.app.repository.MockSessionQuestionRepository mockSessionQuestionRepository;

    @org.springframework.transaction.annotation.Transactional
    public TaskDto createTask(CreateTaskRequest request) {
        String link = request.platformLink() != null ? request.platformLink().trim() : "";
        if (!link.isEmpty() && !link.startsWith("http://") && !link.startsWith("https://")) {
            link = "https://" + link;
        }
        if (link.isEmpty()) {
            link = "https://leetcode.com";
        }

        com.dsabuddies.app.model.Topic topic = request.topicId() != null
                ? topicRepository.findById(request.topicId()).orElse(null)
                : null;

        com.dsabuddies.app.model.TaskSheet sheet = request.taskSheetId() != null
                ? taskSheetRepository.findById(request.taskSheetId()).orElse(null)
                : null;

        Task task = Task.builder()
                .title(request.title().trim())
                .description(request.description() != null ? request.description().trim() : "")
                .difficulty(request.difficulty())
                .platformLink(link)
                .xpReward(request.xpReward())
                .topic(topic)
                .taskSheet(sheet)
                .build();
        task = taskRepository.save(task);

        if (sheet != null && sheet.getTasks() != null) {
            sheet.getTasks().add(task);
        }

        return toDto(task, null);
    }

    public List<TaskDto> getTasksBySheet(Long sheetId, Long currentUserId) {
        return taskRepository.findByTaskSheetId(sheetId).stream()
                .map(t -> toDto(t, currentUserId)).collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByTopic(Long topicId, Long currentUserId) {
        return taskRepository.findByTopicId(topicId).stream()
                .map(t -> toDto(t, currentUserId)).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteTask(Long id) {
        mockSessionQuestionRepository.setTaskNullByTaskId(id);
        taskCompletionRepository.deleteByTaskId(id);
        reviewQueueRepository.deleteByTaskId(id);
        bookmarkRepository.deleteByTaskId(id);
        userNoteRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);
    }

    private TaskDto toDto(Task task, Long currentUserId) {
        boolean completed = currentUserId != null && taskCompletionRepository.existsByUserIdAndTaskId(currentUserId, task.getId());
        return new TaskDto(
                task.getId(), task.getTitle(), task.getDescription(), task.getDifficulty(),
                task.getTopic() != null ? task.getTopic().getName() : null,
                task.getTopic() != null ? task.getTopic().getColor() : null,
                task.getPlatformLink(), task.getXpReward(), completed,
                task.getCompanyTags(), task.getPatternTags()
        );
    }
}
