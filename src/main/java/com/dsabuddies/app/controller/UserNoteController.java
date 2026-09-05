package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.SaveNoteRequest;
import com.dsabuddies.app.dto.UserNoteDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.UserNoteService;
import com.dsabuddies.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserNoteController {

    private final UserNoteService userNoteService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserNoteDto>> getNotes(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(userNoteService.getNotes(user.getId()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<UserNoteDto> getNoteForTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return userNoteService.getNoteForTask(user.getId(), taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<UserNoteDto> saveNote(
            @PathVariable Long taskId,
            @Valid @RequestBody SaveNoteRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(userNoteService.saveNote(user, taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long taskId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        userNoteService.deleteNote(user.getId(), taskId);
        return ResponseEntity.ok().build();
    }
}
