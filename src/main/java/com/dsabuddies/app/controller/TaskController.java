package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.CreateTaskRequest;
import com.dsabuddies.app.dto.TaskDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.AuditService;
import com.dsabuddies.app.service.TaskService;
import com.dsabuddies.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AuditService auditService;

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
    @PreAuthorize("hasAnyRole('ADMIN') or hasAuthority('ROLE_ADMIN') or @userService.isAdmin(authentication)")
    public ResponseEntity<TaskDto> createTask(
            @jakarta.validation.Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal OAuth2User principal,
            org.springframework.security.core.Authentication authentication,
            HttpServletRequest servletRequest) {
        User user = userService.resolveUser(authentication, principal);
        String adminEmail = user != null ? user.getEmail() : UserService.ADMIN_EMAIL;
        String adminName = user != null ? user.getName() : "Admin";

        TaskDto created = taskService.createTask(request);

        auditService.logWithRequest(
                adminEmail,
                adminName,
                "CREATE_TASK",
                "TASK",
                String.valueOf(created.id()),
                "Created task: " + created.title() + " in sheet ID " + request.taskSheetId(),
                servletRequest
        );

        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN') or hasAuthority('ROLE_ADMIN') or @userService.isAdmin(authentication)")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal,
            org.springframework.security.core.Authentication authentication,
            HttpServletRequest servletRequest) {
        User user = userService.resolveUser(authentication, principal);
        String adminEmail = user != null ? user.getEmail() : UserService.ADMIN_EMAIL;
        String adminName = user != null ? user.getName() : "Admin";

        taskService.deleteTask(id);

        auditService.logWithRequest(
                adminEmail,
                adminName,
                "DELETE_TASK",
                "TASK",
                String.valueOf(id),
                "Deleted task ID " + id,
                servletRequest
        );

        return ResponseEntity.ok().build();
    }
    
    private Long getUserId(OAuth2User principal) {
        if (principal == null) return null;
        return userService.getOrCreateUser(principal).getId();
    }
}
