package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.CreateTaskSheetRequest;
import com.dsabuddies.app.dto.TaskSheetDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.AuditService;
import com.dsabuddies.app.service.TaskSheetService;
import com.dsabuddies.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AuditService auditService;

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
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest servletRequest) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getOrCreateUser(principal);
        TaskSheetDto created = taskSheetService.createTaskSheet(request, user);

        auditService.logWithRequest(
                user.getEmail(),
                user.getName(),
                "CREATE_SHEET",
                "TASK_SHEET",
                String.valueOf(created.id()),
                "Created task sheet: " + created.title() + " [" + created.sheetType() + "]",
                servletRequest
        );

        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTaskSheet(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest servletRequest) {
        User user = principal != null ? userService.getOrCreateUser(principal) : null;
        String adminEmail = user != null ? user.getEmail() : "admin@dsabuddies.com";
        String adminName = user != null ? user.getName() : "Admin";

        taskSheetService.deleteTaskSheet(id);

        auditService.logWithRequest(
                adminEmail,
                adminName,
                "DELETE_SHEET",
                "TASK_SHEET",
                String.valueOf(id),
                "Deleted task sheet ID " + id,
                servletRequest
        );

        return ResponseEntity.ok().build();
    }
    
    private Long getUserId(OAuth2User principal) {
        if (principal == null) return null;
        return userService.getOrCreateUser(principal).getId();
    }
}
