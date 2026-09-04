package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.CreateTaskSheetRequest;
import com.dsabuddies.app.dto.TaskSheetDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.UserRepository;
import com.dsabuddies.app.service.TaskSheetService;
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
    private final UserRepository userRepository;

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
            @RequestBody CreateTaskSheetRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        User user = userRepository.findByEmail(principal.getAttribute("email")).orElseThrow();
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
        String email = principal.getAttribute("email");
        return userRepository.findByEmail(email).map(User::getId).orElse(null);
    }
}
