package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.UserDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/members")
    public ResponseEntity<List<UserDto>> getMembers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/members/{id}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal OAuth2User principal) {
        
        String newRole = request != null ? request.get("role") : null;
        if (newRole == null || (!newRole.equals("ROLE_ADMIN") && !newRole.equals("ROLE_USER"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Must be ROLE_USER or ROLE_ADMIN"));
        }

        // Prevent self-demotion to avoid accidental admin lockout
        if (principal != null) {
            User currentUser = userService.getOrCreateUser(principal);
            if (currentUser.getId().equals(id) && !"ROLE_ADMIN".equals(newRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Admins cannot remove their own admin role"));
            }
        }

        userService.updateUserRole(id, newRole);
        return ResponseEntity.ok().build();
    }
}
