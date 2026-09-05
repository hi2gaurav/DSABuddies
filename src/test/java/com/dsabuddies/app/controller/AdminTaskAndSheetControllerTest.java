package com.dsabuddies.app.controller;

import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.repository.TopicRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminTaskAndSheetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TopicRepository topicRepository;

    @Test
    @WithMockUser(username = "hi2gauravgb@gmail.com", roles = "ADMIN")
    @DisplayName("Admin can create task sheet, add task to it with plain URL, and delete both via REST endpoints")
    void testAdminSheetAndTaskEndpoints() throws Exception {
        Topic topic = topicRepository.findAll().stream().findFirst().orElseGet(() ->
                topicRepository.save(Topic.builder().name("Endpoints Topic").color("#000000").icon("⚡").build())
        );

        // 1. Create Task Sheet via POST /api/task-sheets
        String sheetJson = """
                {
                    "title": "Controller Test Sheet",
                    "description": "Created via admin controller test",
                    "startDate": "2026-09-06",
                    "endDate": "2026-09-13",
                    "sheetType": "DAILY"
                }
                """;

        String sheetResponse = mockMvc.perform(post("/api/task-sheets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sheetJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Controller Test Sheet"))
                .andReturn().getResponse().getContentAsString();

        // Extract created sheet id
        com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(sheetResponse);
        long sheetId = rootNode.get("id").asLong();

        // 2. Add Task via POST /api/tasks (using leetcode.com link without protocol to verify URL normalization)
        String taskJson = String.format("""
                {
                    "title": "Normalized Two Sum",
                    "description": "Hash map practice",
                    "difficulty": "EASY",
                    "topicId": %d,
                    "platformLink": "leetcode.com/problems/two-sum",
                    "xpReward": 120,
                    "taskSheetId": %d
                }
                """, topic.getId(), sheetId);

        String taskResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.platformLink").value("https://leetcode.com/problems/two-sum"))
                .andReturn().getResponse().getContentAsString();

        long taskId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(taskResponse).get("id").asLong();

        // 3. Delete Task via DELETE /api/tasks/{id}
        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isOk());

        // 4. Delete Sheet via DELETE /api/task-sheets/{id}
        mockMvc.perform(delete("/api/task-sheets/" + sheetId))
                .andExpect(status().isOk());
    }
}
