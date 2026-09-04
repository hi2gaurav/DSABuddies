package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.CreateTaskSheetRequest;
import com.dsabuddies.app.dto.TaskDto;
import com.dsabuddies.app.dto.TaskSheetDto;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskSheet;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.TaskSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskSheetService {
    private final TaskSheetRepository taskSheetRepository;
    private final TaskCompletionRepository taskCompletionRepository;

    public TaskSheetDto createTaskSheet(CreateTaskSheetRequest request, User createdBy) {
        TaskSheet sheet = TaskSheet.builder()
                .title(request.title())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .sheetType(request.sheetType())
                .createdBy(createdBy)
                .build();
        return toDto(taskSheetRepository.save(sheet), createdBy.getId());
    }

    public TaskSheetDto getTaskSheetById(Long id, Long currentUserId) {
        return toDto(taskSheetRepository.findById(id).orElseThrow(), currentUserId);
    }

    public List<TaskSheetDto> getActiveTaskSheets(Long currentUserId) {
        return taskSheetRepository.findByEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate.now())
                .stream().map(s -> toDto(s, currentUserId)).collect(Collectors.toList());
    }

    public List<TaskSheetDto> getAllTaskSheets(Long currentUserId) {
        return taskSheetRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(s -> toDto(s, currentUserId)).collect(Collectors.toList());
    }

    public void deleteTaskSheet(Long id) {
        taskSheetRepository.deleteById(id);
    }

    private TaskSheetDto toDto(TaskSheet sheet, Long currentUserId) {
        List<TaskDto> taskDtos = sheet.getTasks().stream().map(t -> {
            boolean completed = currentUserId != null && taskCompletionRepository.existsByUserIdAndTaskId(currentUserId, t.getId());
            return new TaskDto(
                    t.getId(), t.getTitle(), t.getDescription(), t.getDifficulty(),
                    t.getTopic() != null ? t.getTopic().getName() : null,
                    t.getTopic() != null ? t.getTopic().getColor() : null,
                    t.getPlatformLink(), t.getXpReward(), completed
            );
        }).collect(Collectors.toList());

        return new TaskSheetDto(
                sheet.getId(), sheet.getTitle(), sheet.getDescription(),
                sheet.getStartDate(), sheet.getEndDate(), sheet.getSheetType(),
                sheet.getCreatedBy() != null ? sheet.getCreatedBy().getName() : null,
                taskDtos, sheet.getCreatedAt()
        );
    }
}
