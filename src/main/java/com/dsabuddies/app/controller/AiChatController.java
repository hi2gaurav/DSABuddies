package com.dsabuddies.app.controller;

import com.dsabuddies.app.service.GeminiService;
import com.dsabuddies.app.service.GeminiService.ChatMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final GeminiService geminiService;

    @Data
    public static class ChatRequest {
        private List<ChatMessage> messages;
    }

    @PostMapping("/jeetu-bhaiya")
    public ResponseEntity<Map<String, String>> chatWithJeetuBhaiya(@RequestBody ChatRequest request) {
        List<ChatMessage> history = request != null && request.getMessages() != null 
                ? request.getMessages() 
                : List.of();
        String reply = geminiService.chatWithJeetuBhaiya(history);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
