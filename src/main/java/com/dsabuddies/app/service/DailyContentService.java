package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.DailyContentDto;
import com.dsabuddies.app.dto.DailyContentDto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DailyContentService {

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    private final Map<String, DailyContentDto> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> refreshOffsets = new ConcurrentHashMap<>();

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void refreshDailyMidnightContent() {
        LocalDate today = LocalDate.now();
        log.info("12:00 AM Midnight Trigger: Rotating and pre-warming daily interview prep content for {}", today);
        cache.clear();
        refreshOffsets.clear();
        getDailyContent(today);
    }

    public DailyContentDto refreshContent(LocalDate date) {
        String key = date.toString();
        int offset = refreshOffsets.compute(key, (k, v) -> v == null ? 1 : v + 1);
        cache.remove(key);
        DailyContentDto content = generateDeterministicDailyContent(date, offset);
        cache.put(key, content);
        return content;
    }

    public DailyContentDto getDailyContent(LocalDate date) {
        String key = date.toString();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        int offset = refreshOffsets.getOrDefault(key, 0);
        DailyContentDto content = generateDeterministicDailyContent(date, offset);
        cache.put(key, content);
        return content;
    }

    private DailyContentDto generateDeterministicDailyContent(LocalDate date, int offset) {
        long seed = date.toEpochDay() + (offset * 7919L);
        Random rng = new Random(seed);
        int dayOfYear = date.getDayOfYear();

        List<LeetCodeProblem> leetCodeBank = getLeetCodeBank();
        List<DesignTopic> lldBank = getLldBank();
        List<DesignTopic> hldBank = getHldBank();
        List<InterviewQuestion> javaBank = getJavaQuestionsBank();
        List<InterviewQuestion> springBank = getSpringBootQuestionsBank();
        List<InterviewQuestion> dbBank = getDatabaseQuestionsBank();
        List<InterviewQuestion> csBank = getCsQuestionsBank();

        LeetCodeProblem leetCode = leetCodeBank.get((int) Math.abs(seed % leetCodeBank.size()));
        DesignTopic lld = lldBank.get((int) Math.abs(seed % lldBank.size()));
        DesignTopic hld = hldBank.get((int) Math.abs(seed % hldBank.size()));

        if (leetCode.getDetailedSolution() == null) {
            leetCode.setDetailedSolution(buildLeetCodeDetailedSolution(leetCode));
        }
        if (leetCode.getCompanies() == null || leetCode.getCompanies().isEmpty()) {
            leetCode.setCompanies(List.of("Amazon", "Google", "Microsoft", "Meta", "Uber"));
        }
        if (lld.getDetailedSolution() == null) {
            lld.setDetailedSolution(buildDesignDetailedSolution(lld));
        }
        if (hld.getDetailedSolution() == null) {
            hld.setDetailedSolution(buildDesignDetailedSolution(hld));
        }

        List<InterviewQuestion> dailyJava = pickDistinctQuestions(javaBank, 10, seed + 101);
        List<InterviewQuestion> dailySpring = pickDistinctQuestions(springBank, 10, seed + 202);
        List<InterviewQuestion> dailyDb = pickDistinctQuestions(dbBank, 10, seed + 303);
        List<InterviewQuestion> dailyCs = pickDistinctQuestions(csBank, 10, seed + 404);

        String[] quotes = {
            "First, solve the problem. Then, write the code. — John Johnson",
            "Simplicity is prerequisite for reliability. — Edsger W. Dijkstra",
            "Make it work, make it right, make it fast. — Kent Beck",
            "Programs must be written for people to read, and only incidentally for machines to execute. — Abelson & Sussman",
            "Any fool can write code that a computer can understand. Good programmers write code that humans can understand. — Martin Fowler",
            "The only way to go fast, is to go well. — Robert C. Martin"
        };
        String quote = quotes[(int) Math.abs(seed % quotes.length)];

        return DailyContentDto.builder()
                .date(date.toString())
                .dayOfYear(dayOfYear)
                .quoteOfTheDay(quote)
                .leetCodeProblem(leetCode)
                .lldTopic(lld)
                .hldTopic(hld)
                .javaQuestions(dailyJava)
                .springBootQuestions(dailySpring)
                .databaseQuestions(dailyDb)
                .csSubjectsQuestions(dailyCs)
                .build();
    }

    private List<InterviewQuestion> pickDistinctQuestions(List<InterviewQuestion> pool, int count, long seed) {
        List<InterviewQuestion> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, new Random(seed));
        List<InterviewQuestion> selected = new ArrayList<>();
        int take = Math.min(count, copy.size());
        for (int i = 0; i < take; i++) {
            InterviewQuestion q = copy.get(i);
            String detailed = (q.getDetailedSolution() != null && !q.getDetailedSolution().isBlank())
                    ? q.getDetailedSolution()
                    : buildComprehensiveDetailedSolution(q);

            selected.add(InterviewQuestion.builder()
                    .id(i + 1)
                    .category(q.getCategory())
                    .topic(q.getTopic())
                    .question(q.getQuestion())
                    .answer(q.getAnswer())
                    .keyPoints(q.getKeyPoints())
                    .codeSnippet(q.getCodeSnippet())
                    .detailedSolution(detailed)
                    .build());
        }
        return selected;
    }

    private String buildComprehensiveDetailedSolution(InterviewQuestion q) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 🎯 Concept & Interview Breakdown\n\n");
        sb.append(q.getAnswer()).append("\n\n");

        sb.append("### ⚙️ Deep-Dive Architecture & Execution Mechanics\n");
        if (q.getKeyPoints() != null && !q.getKeyPoints().isEmpty()) {
            for (String kp : q.getKeyPoints()) {
                sb.append("- **Core Principle**: ").append(kp).append("\n");
            }
            sb.append("\n");
        }

        if (q.getCodeSnippet() != null && !q.getCodeSnippet().isBlank()) {
            sb.append("### 💻 Production Reference Implementation\n```java\n");
            sb.append(q.getCodeSnippet().trim()).append("\n```\n\n");
        }

        sb.append("### ⚡ Interview Edge Cases & High-Frequency Follow-ups\n");
        sb.append("- **Performance & Resource Footprint**: Verify memory allocation, latency trade-offs, and GC pauses under load.\n");
        sb.append("- **Thread Safety**: Ensure synchronization, volatile semantics, or lock-free concurrency where state is shared.\n");
        sb.append("- **Enterprise Pitfall**: Look out for connection leaks, unindexed queries, or unhandled checked exceptions.\n");

        return sb.toString();
    }

    private String buildLeetCodeDetailedSolution(LeetCodeProblem p) {
        return String.format("""
                ### 💡 Intuition & Optimal Strategy
                %s

                ### 📐 Complexity Analysis
                - **Time Complexity**: `%s`
                - **Space Complexity**: `%s`

                ### 🚀 Key Whiteboard Walkthrough
                1. Clarify edge cases: empty input, boundary constraints, nulls, duplicates.
                2. Explain the brute force baseline before diving into the optimal %s.
                3. Trace example step-by-step with dry run tables before coding.

                ### 🎯 Follow-up Questions Typically Asked by FAANG Interviewers
                - How would you handle this if input cannot fit in memory (external sorting/streaming)?
                - Can this be parallelized across multiple cores?
                """,
                p.getOptimalApproach(),
                p.getTimeComplexity(),
                p.getSpaceComplexity(),
                p.getTopic()
        );
    }

    private String buildDesignDetailedSolution(DesignTopic d) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 🏛️ High-Level System Architecture\n\n");
        sb.append(d.getArchitectureSummary()).append("\n\n");

        sb.append("### 📋 Core Functional & Non-Functional Specifications\n");
        if (d.getCoreRequirements() != null) {
            for (String req : d.getCoreRequirements()) {
                sb.append("- ").append(req).append("\n");
            }
            sb.append("\n");
        }

        sb.append("### 🛠️ Key Design Patterns & Components\n");
        if (d.getDesignPatternsOrComponents() != null) {
            for (String pat : d.getDesignPatternsOrComponents()) {
                sb.append("- **Pattern/Component**: ").append(pat).append("\n");
            }
            sb.append("\n");
        }

        sb.append("### ⚖️ High-Scale Trade-offs & Production Bottlenecks\n");
        sb.append("- **Availability vs Consistency**: Choose appropriate database replication and isolation levels.\n");
        sb.append("- **Caching & Invalidation**: Use Redis with write-through or cache-aside, handling cache stampede and thundering herd.\n");
        sb.append("- **Partitioning & Sharding**: Sharding key selection to prevent hot spots and cross-partition queries.\n");

        return sb.toString();
    }

    // ==========================================
    // DELEGATED HIGH-SIGNAL BANKS
    // ==========================================
    List<LeetCodeProblem> getLeetCodeBank() {
        return DailyLeetCodeBank.getAllProblems();
    }

    List<DesignTopic> getLldBank() {
        return DailyDesignBank.getLldTopics();
    }

    List<DesignTopic> getHldBank() {
        return DailyDesignBank.getHldTopics();
    }

    List<InterviewQuestion> getJavaQuestionsBank() {
        return DailyInterviewQuestionsBank.getJavaQuestions();
    }

    List<InterviewQuestion> getSpringBootQuestionsBank() {
        return DailyInterviewQuestionsBank.getSpringBootQuestions();
    }

    List<InterviewQuestion> getDatabaseQuestionsBank() {
        return DailyInterviewQuestionsBank.getDatabaseQuestions();
    }

    List<InterviewQuestion> getCsQuestionsBank() {
        return DailyInterviewQuestionsBank.getCsQuestions();
    }
}
