package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.BookmarkDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.service.BookmarkService;
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
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<BookmarkDto>> getBookmarks(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(bookmarkService.getBookmarks(user.getId()));
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<BookmarkDto> addBookmark(
            @PathVariable Long taskId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(bookmarkService.addBookmark(user, taskId));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable Long taskId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        bookmarkService.removeBookmark(user.getId(), taskId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{taskId}/status")
    public ResponseEntity<Map<String, Boolean>> isBookmarked(
            @PathVariable Long taskId,
            @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getOrCreateUser(principal);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarkService.isBookmarked(user.getId(), taskId)));
    }
}
