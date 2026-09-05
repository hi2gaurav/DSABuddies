package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.*;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.AnnouncementService;
import com.dsabuddies.app.service.AuditService;
import com.dsabuddies.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN') or hasAuthority('ROLE_ADMIN') or @userService.isAdmin(authentication)")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;
    private final AnnouncementService announcementService;

    @GetMapping("/members")
    public ResponseEntity<List<UserDto>> getMembers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/members/{id}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest servletRequest) {
        
        String newRole = request != null ? request.get("role") : null;
        if (newRole == null || (!newRole.equals("ROLE_ADMIN") && !newRole.equals("ROLE_USER"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Must be ROLE_USER or ROLE_ADMIN"));
        }

        User adminUser = principal != null ? userService.getOrCreateUser(principal) : null;
        String adminEmail = adminUser != null ? adminUser.getEmail() : "admin@dsabuddies.com";
        String adminName = adminUser != null ? adminUser.getName() : "Admin";

        // Prevent self-demotion to avoid accidental admin lockout
        if (adminUser != null && adminUser.getId().equals(id) && !"ROLE_ADMIN".equals(newRole)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Admins cannot remove their own admin role"));
        }

        userService.updateUserRole(id, newRole);

        auditService.logWithRequest(
                adminEmail,
                adminName,
                "UPDATE_ROLE",
                "USER",
                String.valueOf(id),
                "Changed role of user ID " + id + " to " + newRole,
                servletRequest
        );

        return ResponseEntity.ok().build();
    }

    @PutMapping("/members/{id}/status")
    public ResponseEntity<?> updateMemberStatus(
            @PathVariable Long id,
            @RequestBody UpdateMemberStatusRequest request,
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest servletRequest) {

        User adminUser = principal != null ? userService.getOrCreateUser(principal) : null;
        String adminEmail = adminUser != null ? adminUser.getEmail() : "admin@dsabuddies.com";
        String adminName = adminUser != null ? adminUser.getName() : "Admin";

        if (adminUser != null && adminUser.getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Admins cannot modify their own moderation status"));
        }

        try {
            UserDto updated = userService.updateMemberStatus(
                    id,
                    request != null ? request.status() : "ACTIVE",
                    request != null ? request.muteDurationHours() : null,
                    request != null ? request.reason() : null
            );

            auditService.logWithRequest(
                    adminEmail,
                    adminName,
                    "STATUS_CHANGE",
                    "USER",
                    String.valueOf(id),
                    "Updated status of user " + updated.name() + " (" + updated.email() + ") to " + updated.status() +
                            (updated.moderationReason() != null ? " [Reason: " + updated.moderationReason() + "]" : ""),
                    servletRequest
            );

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/broadcast")
    public ResponseEntity<AnnouncementDto> broadcastAnnouncement(
            @Valid @RequestBody BroadcastRequest request,
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest servletRequest) {

        User adminUser = principal != null ? userService.getOrCreateUser(principal) : null;
        String adminEmail = adminUser != null ? adminUser.getEmail() : "admin@dsabuddies.com";
        String adminName = adminUser != null ? adminUser.getName() : "Admin";

        AnnouncementDto announcement = announcementService.createAnnouncement(request, adminName);

        auditService.logWithRequest(
                adminEmail,
                adminName,
                "CREATE_ANNOUNCEMENT",
                "ANNOUNCEMENT",
                String.valueOf(announcement.id()),
                "Broadcasted announcement: " + announcement.title() + " [Priority: " + announcement.priority() + "]",
                servletRequest
        );

        return ResponseEntity.ok(announcement);
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementDto>> getAllAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest servletRequest) {

        User adminUser = principal != null ? userService.getOrCreateUser(principal) : null;
        String adminEmail = adminUser != null ? adminUser.getEmail() : "admin@dsabuddies.com";
        String adminName = adminUser != null ? adminUser.getName() : "Admin";

        announcementService.deleteAnnouncement(id);

        auditService.logWithRequest(
                adminEmail,
                adminName,
                "DELETE_ANNOUNCEMENT",
                "ANNOUNCEMENT",
                String.valueOf(id),
                "Deleted announcement ID " + id,
                servletRequest
        );

        return ResponseEntity.ok().build();
    }
}
