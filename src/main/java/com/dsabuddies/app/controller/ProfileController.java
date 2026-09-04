package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.TopicProgressDto;
import com.dsabuddies.app.dto.UserDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.UserRepository;
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
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
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
