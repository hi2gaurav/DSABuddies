package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.BadgeDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.BadgeService;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<BadgeDto>> getAllBadges(@AuthenticationPrincipal OAuth2User principal) {
        Long userId = null;
        if (principal != null) {
            User user = userService.getOrCreateUser(principal);
            userId = user.getId();
        }
        return ResponseEntity.ok(badgeService.getAllBadgesForUser(userId));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<BadgeDto>> getMyBadges(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(badgeService.getEarnedBadges(user.getId()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BadgeDto>> getUserBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(badgeService.getEarnedBadges(userId));
    }
}
