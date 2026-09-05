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

    @Autowired
    private MockSessionRepository mockSessionRepository;

    @Autowired
    private MockSessionQuestionRepository mockSessionQuestionRepository;

    @Autowired
    private TaskService taskService;

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

    @Test
    @DisplayName("Deleting a task sheet with referenced MockSessionQuestion must nullify task reference and delete sheet cleanly")
    void testDeleteTaskSheetWithMockSessionQuestion() {
        User testUser = userRepository.save(User.builder()
                .email("mockcascade_" + System.currentTimeMillis() + "@example.com")
                .name("Mock Cascade Tester")
                .role("ROLE_USER")
                .totalXp(0)
                .currentStreak(0)
                .maxStreak(0)
                .build());

        Topic topic = topicRepository.findAll().stream().findFirst().orElseGet(() ->
                topicRepository.save(Topic.builder().name("DSA Topic").color("#123456").icon("🧩").build())
        );

        TaskSheet sheet = taskSheetRepository.save(TaskSheet.builder()
                .title("Mock Sheet " + System.currentTimeMillis())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .sheetType("DAILY")
                .build());

        Task task = taskRepository.save(Task.builder()
                .title("Mock Problem")
                .difficulty("MEDIUM")
                .topic(topic)
                .xpReward(100)
                .platformLink("https://leetcode.com/problems/mock-prob")
                .taskSheet(sheet)
                .build());

        MockSession session = mockSessionRepository.save(MockSession.builder()
                .user(testUser)
                .mode("DSA")
                .difficultyFilter("MEDIUM")
                .questionCount(1)
                .timeLimitMinutes(45)
                .startedAt(LocalDateTime.now())
                .status("IN_PROGRESS")
                .build());

        MockSessionQuestion q = mockSessionQuestionRepository.save(MockSessionQuestion.builder()
                .session(session)
                .task(task)
                .customTitle(task.getTitle())
                .customDescription("Question for mock interview")
                .difficulty(task.getDifficulty())
                .topicName(topic.getName())
                .questionOrder(1)
                .build());

        assertNotNull(q.getId());
        assertEquals(task.getId(), q.getTask().getId());

        // Deleting the sheet must unlink the task from MockSessionQuestion and delete the sheet
        assertDoesNotThrow(() -> taskSheetService.deleteTaskSheet(sheet.getId()));

        assertTrue(taskSheetRepository.findById(sheet.getId()).isEmpty());
        assertTrue(taskRepository.findById(task.getId()).isEmpty());

        // The mock session question still exists, but its task reference is set to null
        MockSessionQuestion updatedQ = mockSessionQuestionRepository.findById(q.getId()).orElseThrow();
        assertNull(updatedQ.getTask(), "Task reference in MockSessionQuestion must be nullified");
    }

    @Test
    @DisplayName("Deleting an individual task with referenced MockSessionQuestion must nullify task reference and delete task cleanly")
    void testDeleteTaskWithMockSessionQuestion() {
        User testUser = userRepository.save(User.builder()
                .email("singletask_" + System.currentTimeMillis() + "@example.com")
                .name("Single Task Tester")
                .role("ROLE_USER")
                .totalXp(0)
                .currentStreak(0)
                .maxStreak(0)
                .build());

        Topic topic = topicRepository.findAll().stream().findFirst().orElseGet(() ->
                topicRepository.save(Topic.builder().name("Topic Single").color("#123456").icon("📌").build())
        );

        TaskSheet sheet = taskSheetRepository.save(TaskSheet.builder()
                .title("Sheet For Single Task " + System.currentTimeMillis())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .sheetType("DAILY")
                .build());

        Task task = taskRepository.save(Task.builder()
                .title("Single Problem To Delete")
                .difficulty("HARD")
                .topic(topic)
                .xpReward(200)
                .platformLink("https://leetcode.com/problems/single-del")
                .taskSheet(sheet)
                .build());

        MockSession session = mockSessionRepository.save(MockSession.builder()
                .user(testUser)
                .mode("DSA")
                .difficultyFilter("HARD")
                .questionCount(1)
                .timeLimitMinutes(45)
                .startedAt(LocalDateTime.now())
                .status("IN_PROGRESS")
                .build());

        MockSessionQuestion q = mockSessionQuestionRepository.save(MockSessionQuestion.builder()
                .session(session)
                .task(task)
                .customTitle(task.getTitle())
                .customDescription("Single task mock")
                .difficulty(task.getDifficulty())
                .topicName(topic.getName())
                .questionOrder(1)
                .build());

        assertDoesNotThrow(() -> taskService.deleteTask(task.getId()));

        assertTrue(taskRepository.findById(task.getId()).isEmpty());
        MockSessionQuestion updatedQ = mockSessionQuestionRepository.findById(q.getId()).orElseThrow();
        assertNull(updatedQ.getTask(), "Task reference in MockSessionQuestion must be nullified");
    }
}
