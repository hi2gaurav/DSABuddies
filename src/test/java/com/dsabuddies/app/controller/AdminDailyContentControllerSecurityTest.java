package com.dsabuddies.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDailyContentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated request to admin daily-content should be rejected (401 or 403)")
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/daily-content"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(
                            status == 401 || status == 403,
                            "Expected 401 Unauthorized or 403 Forbidden but got " + status
                    );
                });
    }

    @Test
    @WithMockUser(username = "regular@example.com", roles = "USER")
    @DisplayName("Regular user with ROLE_USER should be rejected with 403 Forbidden")
    void testRegularUserForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/daily-content"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hi2gauravgb@gmail.com", roles = "ADMIN")
    @DisplayName("Admin user with ROLE_ADMIN should receive 200 OK with full daily content JSON")
    void testAdminUserAllowed() throws Exception {
        mockMvc.perform(get("/api/admin/daily-content")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").isNotEmpty())
                .andExpect(jsonPath("$.dayOfYear").isNumber())
                .andExpect(jsonPath("$.quoteOfTheDay").isNotEmpty())
                .andExpect(jsonPath("$.leetCodeProblem.title").isNotEmpty())
                .andExpect(jsonPath("$.leetCodeProblem.url").isNotEmpty())
                .andExpect(jsonPath("$.lldTopic.title").isNotEmpty())
                .andExpect(jsonPath("$.lldTopic.type", is("LLD")))
                .andExpect(jsonPath("$.hldTopic.title").isNotEmpty())
                .andExpect(jsonPath("$.hldTopic.type", is("HLD")))
                .andExpect(jsonPath("$.javaQuestions", hasSize(10)))
                .andExpect(jsonPath("$.springBootQuestions", hasSize(10)))
                .andExpect(jsonPath("$.databaseQuestions", hasSize(10)))
                .andExpect(jsonPath("$.csSubjectsQuestions", hasSize(10)));
    }
}
