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
public class AdminDailyContentController {

    private final DailyContentService dailyContentService;

    @GetMapping("/daily-content")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DailyContentDto> getDailyContent(
            @RequestParam(value = "date", required = false) String dateStr) {
        LocalDate date = (dateStr != null && !dateStr.isBlank()) 
                ? LocalDate.parse(dateStr) 
                : LocalDate.now();
        DailyContentDto content = dailyContentService.getDailyContent(date);
        return ResponseEntity.ok(content);
    }
}
