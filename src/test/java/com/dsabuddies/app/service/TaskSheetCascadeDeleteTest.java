package com.dsabuddies.app.service;

import com.dsabuddies.app.model.*;
import com.dsabuddies.app.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskSheetCascadeDeleteTest {

    @Autowired
    private TaskSheetService taskSheetService;

    @Autowired
    private TaskSheetRepository taskSheetRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskCompletionRepository taskCompletionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deleting a task sheet must cascade and delete task completions without foreign key constraint violation")
    void testDeleteTaskSheetWithCompletions() {
        // 1. Ensure user and topic exist
        String testEmail = "testcascade_" + System.currentTimeMillis() + "@example.com";
        User testUser = userRepository.save(User.builder()
                .email(testEmail)
                .name("Cascade Tester")
                .role("ROLE_USER")
                .totalXp(0)
                .currentStreak(0)
                .maxStreak(0)
                .build());

        Topic topic = topicRepository.findAll().stream().findFirst().orElseGet(() ->
                topicRepository.save(Topic.builder().name("Test Topic").color("#123456").icon("🧪").build())
        );

        // 2. Create a task sheet with a task
        TaskSheet sheet = TaskSheet.builder()
                .title("Temporary Sheet for Cascade Testing")
                .description("Testing cascade removal")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .sheetType("DAILY")
                .build();
        sheet = taskSheetRepository.save(sheet);

        Task task = Task.builder()
                .title("Temporary Task")
                .difficulty("EASY")
                .topic(topic)
                .xpReward(50)
                .platformLink("https://leetcode.com/problems/test")
                .taskSheet(sheet)
                .build();
        task = taskRepository.save(task);

        // 3. Create a task completion record referencing the task
        TaskCompletion completion = TaskCompletion.builder()
                .user(testUser)
                .task(task)
                .completedAt(LocalDateTime.now())
                .solutionLink("https://github.com/test/sol")
                .notes("Testing cascade notes")
                .build();
        completion = taskCompletionRepository.save(completion);

        assertNotNull(completion.getId());
        assertEquals(1, taskCompletionRepository.findByTaskId(task.getId()).size());

        // 4. Delete the task sheet - this previously failed with SQL foreign key constraint violation!
        final Long sheetId = sheet.getId();
        assertDoesNotThrow(() -> taskSheetService.deleteTaskSheet(sheetId),
                "deleteTaskSheet should execute cleanly without throwing foreign key constraint errors");

        // 5. Verify the sheet, task, and completion are all cleaned up
        assertTrue(taskSheetRepository.findById(sheet.getId()).isEmpty(), "Sheet should be deleted");
        assertTrue(taskRepository.findById(task.getId()).isEmpty(), "Task should be deleted by orphanRemoval");
        assertTrue(taskCompletionRepository.findById(completion.getId()).isEmpty(), "Completion record should be deleted");
    }
}
