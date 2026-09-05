package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.MockSessionDto;
import com.dsabuddies.app.dto.StartMockRequest;
import com.dsabuddies.app.dto.SubmitMockAnswerRequest;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.MockInterviewService;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mock")
@RequiredArgsConstructor
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;
    private final UserService userService;

    @PostMapping("/start")
    public ResponseEntity<MockSessionDto> startSession(
            @RequestBody(required = false) StartMockRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(mockInterviewService.startSession(user.getId(), request));
    }

    @PutMapping("/{sessionId}/answer/{questionId}")
    public ResponseEntity<MockSessionDto> submitAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody(required = false) SubmitMockAnswerRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(mockInterviewService.submitAnswer(sessionId, questionId, request, user.getId()));
    }

    @PutMapping("/{sessionId}/complete")
    public ResponseEntity<MockSessionDto> completeSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(mockInterviewService.completeSession(sessionId, user.getId()));
    }

    @PutMapping("/{sessionId}/abandon")
    public ResponseEntity<MockSessionDto> abandonSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(mockInterviewService.abandonSession(sessionId, user.getId()));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<MockSessionDto> getSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(mockInterviewService.getSession(sessionId, user.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<MockSessionDto>> getHistory(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(mockInterviewService.getUserSessions(user.getId()));
    }
}
