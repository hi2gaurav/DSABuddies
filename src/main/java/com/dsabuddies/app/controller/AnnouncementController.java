package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.AnnouncementDto;
import com.dsabuddies.app.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementDto>> getActiveAnnouncements() {
        return ResponseEntity.ok(announcementService.getActiveAnnouncements());
    }
}
