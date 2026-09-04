package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.UserDto;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        userService.updateUserRole(id, request.get("role"));
        return ResponseEntity.ok().build();
    }
}
