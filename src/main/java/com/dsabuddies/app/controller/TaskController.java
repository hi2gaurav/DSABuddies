package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.CreateTaskRequest;
import com.dsabuddies.app.dto.TaskDto;
import com.dsabuddies.app.service.TaskService;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam(required = false) Long sheetId,
            @RequestParam(required = false) Long topicId,
            @AuthenticationPrincipal OAuth2User principal) {
        
        Long userId = getUserId(principal);
        
        if (sheetId != null) {
            return ResponseEntity.ok(taskService.getTasksBySheet(sheetId, userId));
        } else if (topicId != null) {
            return ResponseEntity.ok(taskService.getTasksByTopic(topicId, userId));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskDto> createTask(@jakarta.validation.Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }
    
    private Long getUserId(OAuth2User principal) {
        if (principal == null) return null;
        return userService.getOrCreateUser(principal).getId();
    }
}
