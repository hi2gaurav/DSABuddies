package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.SaveNoteRequest;
import com.dsabuddies.app.dto.UserNoteDto;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.model.UserNote;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.UserNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNoteService {

    private final UserNoteRepository userNoteRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<UserNoteDto> getNotes(Long userId) {
        return userNoteRepository.findByUserIdWithTask(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserNoteDto> getNoteForTask(Long userId, Long taskId) {
        return userNoteRepository.findByUserIdAndTaskId(userId, taskId).map(this::toDto);
    }

    @Transactional
    public UserNoteDto saveNote(User user, Long taskId, SaveNoteRequest request) {
        UserNote note = userNoteRepository.findByUserIdAndTaskId(user.getId(), taskId)
                .orElse(null);

        if (note == null) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
            note = UserNote.builder()
                    .user(user)
                    .task(task)
                    .content(request.content())
                    .codeSnippet(request.codeSnippet())
                    .language(request.language() != null ? request.language() : "java")
                    .build();
        } else {
            note.setContent(request.content());
            note.setCodeSnippet(request.codeSnippet());
            if (request.language() != null) {
                note.setLanguage(request.language());
            }
        }

        return toDto(userNoteRepository.save(note));
    }

    @Transactional
    public void deleteNote(Long userId, Long taskId) {
        userNoteRepository.deleteByUserIdAndTaskId(userId, taskId);
    }

    private UserNoteDto toDto(UserNote un) {
        return new UserNoteDto(
                un.getId(),
                un.getTask().getId(),
                un.getTask().getTitle(),
                un.getContent(),
                un.getCodeSnippet(),
                un.getLanguage(),
                un.getUpdatedAt()
        );
    }
}
