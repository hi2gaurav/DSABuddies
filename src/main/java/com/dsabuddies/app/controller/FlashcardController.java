package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.FlashcardDto;
import com.dsabuddies.app.dto.SubmitFlashcardReviewRequest;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.FlashcardService;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<FlashcardDto>> getFlashcards(
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal OAuth2User principal) {
        Long userId = null;
        if (principal != null) {
            User user = userService.getOrCreateUser(principal);
            userId = user.getId();
        }
        return ResponseEntity.ok(flashcardService.getFlashcards(category, userId));
    }

    @GetMapping("/due")
    public ResponseEntity<List<FlashcardDto>> getDueFlashcards(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(flashcardService.getDueFlashcards(user.getId()));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<FlashcardDto> submitReview(
            @PathVariable Long id,
            @RequestBody SubmitFlashcardReviewRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        int rating = request != null ? Math.max(1, Math.min(5, request.rating())) : 3;
        return ResponseEntity.ok(flashcardService.submitFlashcardReview(id, user.getId(), rating));
    }
}
