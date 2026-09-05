package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.ReviewItemDto;
import com.dsabuddies.app.dto.SubmitReviewRequest;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.SpacedRepetitionService;
import com.dsabuddies.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReviewQueueController {

    private final SpacedRepetitionService spacedRepetitionService;
    private final UserService userService;

    @GetMapping("/due")
    public ResponseEntity<List<ReviewItemDto>> getDueReviews(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(spacedRepetitionService.getDueReviews(user.getId()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ReviewItemDto>> getUpcomingReviews(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(spacedRepetitionService.getUpcomingReviews(user.getId()));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getDueCount(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(Map.of("dueCount", spacedRepetitionService.getDueCount(user.getId())));
    }

    @PostMapping("/{taskId}/submit")
    public ResponseEntity<ReviewItemDto> submitReview(
            @PathVariable Long taskId,
            @Valid @RequestBody SubmitReviewRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(spacedRepetitionService.submitReview(user.getId(), taskId, request.rating()));
    }
}
