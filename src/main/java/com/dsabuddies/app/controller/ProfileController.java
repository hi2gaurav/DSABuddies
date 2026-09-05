package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.TopicProgressDto;
import com.dsabuddies.app.dto.UserDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.StatsService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final StatsService statsService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(userService.toDto(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {
        UserDto userDto = userService.getUserById(id);
        User currentUser = principal != null ? userService.getOrCreateUser(principal) : null;
        boolean isMe = currentUser != null && currentUser.getId().equals(id);
        boolean isAdmin = currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole());

        if (!isMe && !isAdmin) {
            // Mask private email address for other community members to prevent data exposure
            userDto = new UserDto(
                    userDto.id(),
                    null,
                    userDto.name(),
                    userDto.avatarUrl(),
                    userDto.role(),
                    userDto.currentStreak(),
                    userDto.maxStreak(),
                    userDto.totalXp(),
                    userDto.createdAt()
            );
        }
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}/topics")
    public ResponseEntity<List<TopicProgressDto>> getTopicProgress(@PathVariable Long id) {
        return ResponseEntity.ok(statsService.getTopicProgress(id));
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<Map<String, Integer>> getActivityData(@PathVariable Long id) {
        return ResponseEntity.ok(statsService.getActivityData(id, 12));
    }
}
