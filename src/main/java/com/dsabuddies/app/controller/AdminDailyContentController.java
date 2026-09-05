package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.DailyContentDto;
import com.dsabuddies.app.service.DailyContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN') or hasAuthority('ROLE_ADMIN') or @userService.isAdmin(authentication)")
public class AdminDailyContentController {

    private final DailyContentService dailyContentService;

    @GetMapping("/daily-content")
    public ResponseEntity<DailyContentDto> getDailyContent(
            @RequestParam(value = "date", required = false) String dateStr) {
        LocalDate date = (dateStr != null && !dateStr.isBlank()) 
                ? LocalDate.parse(dateStr) 
                : LocalDate.now();
        DailyContentDto content = dailyContentService.getDailyContent(date);
        return ResponseEntity.ok(content);
    }

    @org.springframework.web.bind.annotation.PostMapping("/daily-content/refresh")
    public ResponseEntity<DailyContentDto> refreshDailyContent(
            @RequestParam(value = "date", required = false) String dateStr) {
        LocalDate date = (dateStr != null && !dateStr.isBlank()) 
                ? LocalDate.parse(dateStr) 
                : LocalDate.now();
        DailyContentDto content = dailyContentService.refreshContent(date);
        return ResponseEntity.ok(content);
    }
}
