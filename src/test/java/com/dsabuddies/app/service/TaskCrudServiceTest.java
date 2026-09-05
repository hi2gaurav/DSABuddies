package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.CreateTaskRequest;
import com.dsabuddies.app.dto.CreateTaskSheetRequest;
import com.dsabuddies.app.dto.TaskDto;
import com.dsabuddies.app.dto.TaskSheetDto;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.TaskSheetRepository;
import com.dsabuddies.app.repository.TopicRepository;
import com.dsabuddies.app.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskCrudServiceTest {

    @Autowired
    private TaskSheetService taskSheetService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSheetRepository taskSheetRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should successfully create sheet, add task to it, and delete task cleanly")
    void testCreateSheetAndTaskCrud() {
        Topic topic = topicRepository.findAll().stream().findFirst().orElseGet(() ->
                topicRepository.save(Topic.builder().name("General DSA").color("#3B82F6").icon("💡").build())
        );

        User adminUser = userRepository.findAll().stream()
                .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
                .findFirst()
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin_crud_" + System.currentTimeMillis() + "@example.com")
                        .name("Admin User")
                        .role("ROLE_ADMIN")
                        .totalXp(0)
                        .currentStreak(0)
                        .maxStreak(0)
                        .build()));

        // 1. Create Task Sheet
        String title = "CRUD Test Sheet " + System.currentTimeMillis();
        CreateTaskSheetRequest sheetReq = new CreateTaskSheetRequest(
                title,
                "Testing adding sheet and tasks",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                "DAILY"
        );

        TaskSheetDto createdSheet = taskSheetService.createTaskSheet(sheetReq, adminUser);
        assertNotNull(createdSheet);
        assertNotNull(createdSheet.id());
        assertEquals(title, createdSheet.title());

        // 2. Add Task to the Sheet
        CreateTaskRequest taskReq = new CreateTaskRequest(
                "Test Problem 101",
                "Problem description",
                "EASY",
                topic.getId(),
                "https://leetcode.com/problems/two-sum/",
                150,
                createdSheet.id()
        );

        TaskDto createdTask = taskService.createTask(taskReq);
        assertNotNull(createdTask);
        assertNotNull(createdTask.id());
        assertEquals("Test Problem 101", createdTask.title());
        assertEquals(150, createdTask.xpReward());

        // 3. Delete Task directly
        assertDoesNotThrow(() -> taskService.deleteTask(createdTask.id()));
        assertTrue(taskRepository.findById(createdTask.id()).isEmpty(), "Task must be deleted");

        // 4. Delete Sheet
        assertDoesNotThrow(() -> taskSheetService.deleteTaskSheet(createdSheet.id()));
        assertTrue(taskSheetRepository.findById(createdSheet.id()).isEmpty(), "Task sheet must be deleted");
    }
}
