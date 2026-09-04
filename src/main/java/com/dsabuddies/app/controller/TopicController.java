package com.dsabuddies.app.controller;

import com.dsabuddies.app.dto.TopicDto;
import com.dsabuddies.app.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<List<TopicDto>> getTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TopicDto> createTopic(@RequestBody TopicDto request) {
        return ResponseEntity.ok(topicService.createTopic(request.name(), request.color(), request.icon()));
    }
}
