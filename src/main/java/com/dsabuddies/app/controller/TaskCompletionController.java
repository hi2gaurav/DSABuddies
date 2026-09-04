package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.CompleteTaskRequest;
import com.dsabuddies.app.dto.TaskCompletionDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.UserRepository;
import com.dsabuddies.app.service.TaskCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskCompletionController {

    private final TaskCompletionService taskCompletionService;
    private final UserRepository userRepository;

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<TaskCompletionDto> completeTask(
            @PathVariable Long taskId,
            @RequestBody(required = false) CompleteTaskRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        User user = userRepository.findByEmail(principal.getAttribute("email")).orElseThrow();
        return ResponseEntity.ok(taskCompletionService.completeTask(taskId, user.getId(), request));
    }

    @DeleteMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> uncompleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal OAuth2User principal) {
        User user = userRepository.findByEmail(principal.getAttribute("email")).orElseThrow();
        taskCompletionService.uncompleteTask(taskId, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/completions")
    public ResponseEntity<List<TaskCompletionDto>> getCompletions(@AuthenticationPrincipal OAuth2User principal) {
        User user = userRepository.findByEmail(principal.getAttribute("email")).orElseThrow();
        return ResponseEntity.ok(taskCompletionService.getUserCompletions(user.getId()));
    }
}
