package com.dsabuddies.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "coder@example.com", roles = "USER")
    @DisplayName("Authenticated user can chat with Jeetu Bhaiya AI mentor")
    void testChatWithJeetuBhaiya() throws Exception {
        String jsonPayload = """
                {
                    "messages": [
                        { "role": "user", "content": "Jeetu Bhaiya LRU Cache ka concept bata do" }
                    ]
                }
                """;

        mockMvc.perform(post("/api/ai/jeetu-bhaiya")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").isString());
    }
}
