package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.CreateTaskSheetRequest;
import com.dsabuddies.app.dto.TaskSheetDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.TaskSheetService;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-sheets")
@RequiredArgsConstructor
public class TaskSheetController {

    private final TaskSheetService taskSheetService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<TaskSheetDto>> getTaskSheets(
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal OAuth2User principal) {
        
        Long userId = getUserId(principal);
        
        if (Boolean.TRUE.equals(active)) {
            return ResponseEntity.ok(taskSheetService.getActiveTaskSheets(userId));
        }
        return ResponseEntity.ok(taskSheetService.getAllTaskSheets(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskSheetDto> getTaskSheet(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(taskSheetService.getTaskSheetById(id, getUserId(principal)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskSheetDto> createTaskSheet(
            @jakarta.validation.Valid @RequestBody CreateTaskSheetRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(taskSheetService.createTaskSheet(request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTaskSheet(@PathVariable Long id) {
        taskSheetService.deleteTaskSheet(id);
        return ResponseEntity.ok().build();
    }
    
    private Long getUserId(OAuth2User principal) {
        if (principal == null) return null;
        return userService.getOrCreateUser(principal).getId();
    }
}
