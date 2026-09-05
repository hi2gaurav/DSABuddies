package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.DesignTemplateDto;
import com.dsabuddies.app.dto.SaveDesignRequest;
import com.dsabuddies.app.dto.UserDesignDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.DesignTemplateService;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designs")
@RequiredArgsConstructor
public class DesignTemplateController {

    private final DesignTemplateService designTemplateService;
    private final UserService userService;

    @GetMapping("/templates")
    public ResponseEntity<List<DesignTemplateDto>> getTemplates(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(designTemplateService.getAllTemplates(category));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<DesignTemplateDto> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(designTemplateService.getTemplate(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<UserDesignDto>> getMyDesigns(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(designTemplateService.getUserDesigns(user.getId()));
    }

    @PostMapping
    public ResponseEntity<UserDesignDto> saveDesign(
            @RequestBody SaveDesignRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(designTemplateService.saveUserDesign(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDesignDto> updateDesign(
            @PathVariable Long id,
            @RequestBody SaveDesignRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(designTemplateService.updateUserDesign(id, user.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesign(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        designTemplateService.deleteUserDesign(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
