package com.dsabuddies.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyContentDto {
    private String date;
    private int dayOfYear;
    private String quoteOfTheDay;
    private LeetCodeProblem leetCodeProblem;
    private DesignTopic lldTopic;
    private DesignTopic hldTopic;
    private List<InterviewQuestion> javaQuestions;
    private List<InterviewQuestion> springBootQuestions;
    private List<InterviewQuestion> databaseQuestions;
    private List<InterviewQuestion> csSubjectsQuestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeetCodeProblem {
        private String id;
        private String title;
        private String difficulty; // EASY, MEDIUM, HARD
        private String topic;
        private String url;
        private String problemSummary;
        private String optimalApproach;
        private String timeComplexity;
        private String spaceComplexity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DesignTopic {
        private String id;
        private String title;
        private String type; // LLD or HLD
        private String difficulty;
        private String description;
        private List<String> coreRequirements;
        private List<String> designPatternsOrComponents;
        private String architectureSummary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewQuestion {
        private int id;
        private String category;
        private String topic;
        private String question;
        private String answer;
        private List<String> keyPoints;
        private String codeSnippet;
    }
}
