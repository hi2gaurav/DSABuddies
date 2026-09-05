package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.CompleteTaskRequest;
import com.dsabuddies.app.dto.TaskCompletionDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.TaskCompletionService;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TaskCompletionController {

    private final TaskCompletionService taskCompletionService;
    private final UserService userService;

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<TaskCompletionDto> completeTask(
            @PathVariable Long taskId,
            @RequestBody(required = false) CompleteTaskRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(taskCompletionService.completeTask(taskId, user.getId(), request));
    }

    @DeleteMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> uncompleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getOrCreateUser(principal);
        taskCompletionService.uncompleteTask(taskId, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/completions")
    public ResponseEntity<List<TaskCompletionDto>> getCompletions(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(taskCompletionService.getUserCompletions(user.getId()));
    }
}
