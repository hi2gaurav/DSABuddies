package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.DailyContentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DailyContentServiceTest {

    private DailyContentService dailyContentService;

    @BeforeEach
    void setUp() {
        dailyContentService = new DailyContentService();
    }

    @Test
    @DisplayName("Should return complete daily content with all required sections")
    void testDailyContentStructure() {
        LocalDate today = LocalDate.now();
        DailyContentDto content = dailyContentService.getDailyContent(today);

        assertNotNull(content, "Daily content should not be null");
        assertEquals(today.toString(), content.getDate());
        assertEquals(today.getDayOfYear(), content.getDayOfYear());
        assertNotNull(content.getQuoteOfTheDay(), "Quote of the day should be present");
        assertFalse(content.getQuoteOfTheDay().isBlank());

        // LeetCode verification
        assertNotNull(content.getLeetCodeProblem(), "LeetCode problem of the day should be present");
        assertNotNull(content.getLeetCodeProblem().getTitle());
        assertNotNull(content.getLeetCodeProblem().getUrl());
        assertNotNull(content.getLeetCodeProblem().getDifficulty());
        assertNotNull(content.getLeetCodeProblem().getOptimalApproach());
        assertNotNull(content.getLeetCodeProblem().getTimeComplexity());
        assertNotNull(content.getLeetCodeProblem().getSpaceComplexity());

        // LLD verification
        assertNotNull(content.getLldTopic(), "LLD topic should be present");
        assertNotNull(content.getLldTopic().getTitle());
        assertEquals("LLD", content.getLldTopic().getType());
        assertFalse(content.getLldTopic().getCoreRequirements().isEmpty());
        assertFalse(content.getLldTopic().getDesignPatternsOrComponents().isEmpty());
        assertNotNull(content.getLldTopic().getArchitectureSummary());

        // HLD verification
        assertNotNull(content.getHldTopic(), "HLD topic should be present");
        assertNotNull(content.getHldTopic().getTitle());
        assertEquals("HLD", content.getHldTopic().getType());
        assertFalse(content.getHldTopic().getCoreRequirements().isEmpty());
        assertFalse(content.getHldTopic().getDesignPatternsOrComponents().isEmpty());
        assertNotNull(content.getHldTopic().getArchitectureSummary());

        // 10 Questions each verification
        assertNotNull(content.getJavaQuestions());
        assertEquals(10, content.getJavaQuestions().size(), "Must have exactly 10 Java questions");

        assertNotNull(content.getSpringBootQuestions());
        assertEquals(10, content.getSpringBootQuestions().size(), "Must have exactly 10 Spring Boot questions");

        assertNotNull(content.getDatabaseQuestions());
        assertEquals(10, content.getDatabaseQuestions().size(), "Must have exactly 10 Database questions");

        assertNotNull(content.getCsSubjectsQuestions());
        assertEquals(10, content.getCsSubjectsQuestions().size(), "Must have exactly 10 CS Subjects questions");
    }

    @Test
    @DisplayName("Should ensure all 10 questions per category have unique questions and non-blank answers")
    void testQuestionUniquenessAndQuality() {
        LocalDate date = LocalDate.of(2026, 9, 5);
        DailyContentDto content = dailyContentService.getDailyContent(date);

        // Verify Java questions
        verifyQuestionList(content.getJavaQuestions(), "Java Core");

        // Verify Spring Boot questions
        verifyQuestionList(content.getSpringBootQuestions(), "Spring Boot");

        // Verify Database questions
        verifyQuestionList(content.getDatabaseQuestions(), "Database & SQL");

        // Verify CS Subjects questions
        verifyQuestionList(content.getCsSubjectsQuestions(), "CS Subjects");
    }

    private void verifyQuestionList(java.util.List<DailyContentDto.InterviewQuestion> questions, String expectedCategory) {
        assertEquals(10, questions.size(), "List must contain exactly 10 items");
        Set<String> seenQuestions = new HashSet<>();

        for (int i = 0; i < questions.size(); i++) {
            DailyContentDto.InterviewQuestion q = questions.get(i);
            assertEquals(i + 1, q.getId(), "Question IDs must be 1-indexed sequential");
            assertEquals(expectedCategory, q.getCategory());
            assertNotNull(q.getTopic(), "Topic must be set");
            assertNotNull(q.getQuestion(), "Question text must not be null");
            assertFalse(q.getQuestion().isBlank(), "Question text must not be blank");
            assertNotNull(q.getAnswer(), "Answer text must not be null");
            assertFalse(q.getAnswer().isBlank(), "Answer text must not be blank");

            assertTrue(seenQuestions.add(q.getQuestion()), "Questions within the same daily set must be unique: " + q.getQuestion());
        }
    }

    @Test
    @DisplayName("Should cache daily content for the same date")
    void testDailyContentCaching() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        DailyContentDto first = dailyContentService.getDailyContent(date);
        DailyContentDto second = dailyContentService.getDailyContent(date);

        assertSame(first, second, "Should return cached instance for the same date");
    }

    @Test
    @DisplayName("Should rotate smoothly across multiple dates without errors")
    void testMultiDateRotation() {
        for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
            LocalDate date = LocalDate.of(2026, 1, 1).plusDays(dayOffset);
            DailyContentDto content = dailyContentService.getDailyContent(date);
            assertNotNull(content);
            assertEquals(10, content.getJavaQuestions().size());
            assertEquals(10, content.getSpringBootQuestions().size());
            assertEquals(10, content.getDatabaseQuestions().size());
            assertEquals(10, content.getCsSubjectsQuestions().size());
        }
    }

    @Test
    @DisplayName("Should ensure detailedSolution is present across all questions, LeetCode, LLD, and HLD")
    void testDetailedSolutionsPresent() {
        LocalDate date = LocalDate.now();
        DailyContentDto content = dailyContentService.getDailyContent(date);

        assertNotNull(content.getLeetCodeProblem().getDetailedSolution(), "LeetCode detailed solution must be present");
        assertFalse(content.getLeetCodeProblem().getDetailedSolution().isBlank());

        assertNotNull(content.getLldTopic().getDetailedSolution(), "LLD detailed solution must be present");
        assertFalse(content.getLldTopic().getDetailedSolution().isBlank());

        assertNotNull(content.getHldTopic().getDetailedSolution(), "HLD detailed solution must be present");
        assertFalse(content.getHldTopic().getDetailedSolution().isBlank());

        for (DailyContentDto.InterviewQuestion q : content.getJavaQuestions()) {
            assertNotNull(q.getDetailedSolution(), "Java question detailed solution must be present");
            assertFalse(q.getDetailedSolution().isBlank());
        }
    }

    @Test
    @DisplayName("Should support midnight cache refresh and manual refresh")
    void testMidnightAndManualRefresh() {
        LocalDate today = LocalDate.now();
        DailyContentDto initial = dailyContentService.getDailyContent(today);

        dailyContentService.refreshDailyMidnightContent();
        DailyContentDto afterMidnight = dailyContentService.getDailyContent(today);
        assertNotNull(afterMidnight);

        DailyContentDto refreshed = dailyContentService.refreshContent(today);
        assertNotNull(refreshed);
        assertNotEquals(initial.getLeetCodeProblem().getId(), refreshed.getLeetCodeProblem().getId(),
                "Manual refresh must rotate to a fresh LeetCode problem");
    }

    @Test
    @DisplayName("Should verify full code solutions and structured FAANG candidate walkthroughs across all banks")
    void testFaangDetailedSolutionsAndCodeQuality() {
        // 1. Verify all LeetCode problems have complete Java code and structured sections
        for (DailyContentDto.LeetCodeProblem lc : DailyLeetCodeBank.getAllProblems()) {
            assertNotNull(lc.getDetailedSolution(), "LeetCode problem " + lc.getId() + " must have detailed solution");
            assertTrue(lc.getDetailedSolution().contains("```java"), "Must contain full Java code block in " + lc.getId());
            assertTrue(lc.getDetailedSolution().contains("class "), "Must contain Java class definition in " + lc.getId());
            assertTrue(lc.getDetailedSolution().contains("Google Candidate Pitch"), "Must contain Google Candidate pitch in " + lc.getId());
            assertTrue(lc.getDetailedSolution().contains("Complexity"), "Must contain complexity analysis in " + lc.getId());
        }

        // 2. Verify all LLD topics have full Java code and trade-offs
        for (DailyContentDto.DesignTopic lld : DailyDesignBank.getLldTopics()) {
            assertNotNull(lld.getDetailedSolution(), "LLD topic " + lld.getId() + " must have detailed solution");
            assertTrue(lld.getDetailedSolution().contains("```java"), "Must contain Java code block in " + lld.getId());
            assertTrue(lld.getDetailedSolution().contains("class "), "Must define OOP classes in " + lld.getId());
        }

        // 3. Verify all HLD topics have schemas, API contracts, and calculations
        for (DailyContentDto.DesignTopic hld : DailyDesignBank.getHldTopics()) {
            assertNotNull(hld.getDetailedSolution(), "HLD topic " + hld.getId() + " must have detailed solution");
            assertTrue(hld.getDetailedSolution().contains("Scale & Back-of-the-Envelope"), "Must contain scale estimation in " + hld.getId());
            assertTrue(hld.getDetailedSolution().contains("```"), "Must contain schema/code block in " + hld.getId());
        }

        // 4. Verify all 40 questions across categories have code snippets and structured detailed solutions with code examples
        java.util.List<java.util.List<DailyContentDto.InterviewQuestion>> questionBanks = java.util.List.of(
            DailyInterviewQuestionsBank.getJavaQuestions(),
            DailyInterviewQuestionsBank.getSpringBootQuestions(),
            DailyInterviewQuestionsBank.getDatabaseQuestions(),
            DailyInterviewQuestionsBank.getCsQuestions()
        );

        for (java.util.List<DailyContentDto.InterviewQuestion> bank : questionBanks) {
            for (DailyContentDto.InterviewQuestion q : bank) {
                assertNotNull(q.getCodeSnippet(), "Question: " + q.getQuestion() + " must have a codeSnippet");
                assertFalse(q.getCodeSnippet().isBlank(), "codeSnippet must not be blank for: " + q.getQuestion());

                assertNotNull(q.getDetailedSolution(), "detailedSolution must not be null for: " + q.getQuestion());
                assertFalse(q.getDetailedSolution().isBlank(), "detailedSolution must not be blank for: " + q.getQuestion());
                assertTrue(q.getDetailedSolution().contains("Google Candidate Pitch"), "Must have Google pitch for: " + q.getQuestion());
            }
        }
    }
}
