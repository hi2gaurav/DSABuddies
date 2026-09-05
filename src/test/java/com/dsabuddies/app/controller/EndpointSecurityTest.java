package com.dsabuddies.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "regular@example.com", roles = "USER")
    @DisplayName("ROLE_USER must be forbidden from creating task sheets (POST /api/task-sheets)")
    void testCreateSheetForbiddenForUser() throws Exception {
        mockMvc.perform(post("/api/task-sheets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hacked Sheet\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "regular@example.com", roles = "USER")
    @DisplayName("ROLE_USER must be forbidden from deleting task sheets (DELETE /api/task-sheets/1)")
    void testDeleteSheetForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/api/task-sheets/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "regular@example.com", roles = "USER")
    @DisplayName("ROLE_USER must be forbidden from creating tasks (POST /api/tasks)")
    void testCreateTaskForbiddenForUser() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hacked Task\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "regular@example.com", roles = "USER")
    @DisplayName("ROLE_USER must be forbidden from viewing admin members list (/api/admin/members)")
    void testAdminMembersForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("ROLE_ADMIN is allowed to view admin members list")
    void testAdminMembersAllowedForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isOk());
    }
}
