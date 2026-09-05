package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.AdaptiveSuggestionDto;
import com.dsabuddies.app.dto.PatternStatDto;
import com.dsabuddies.app.dto.WeakTopicDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.UserService;
import com.dsabuddies.app.service.WeakAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AnalyticsController {

    private final WeakAreaService weakAreaService;
    private final UserService userService;

    @GetMapping("/weak-topics")
    public ResponseEntity<List<WeakTopicDto>> getWeakTopics(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(weakAreaService.getWeakTopics(user.getId()));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<AdaptiveSuggestionDto>> getAdaptiveSuggestions(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(weakAreaService.getAdaptiveSuggestions(user.getId()));
    }

    @GetMapping("/patterns")
    public ResponseEntity<List<PatternStatDto>> getPatternStats(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(weakAreaService.getPatternStats(user.getId()));
    }
}
