package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.UserDto;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String email = principal.getAttribute("email");
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getAuthStatus(@AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(Map.of("authenticated", principal != null));
    }
}
