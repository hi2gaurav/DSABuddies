package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.MockQuestionDto;
import com.dsabuddies.app.dto.MockSessionDto;
import com.dsabuddies.app.dto.StartMockRequest;
import com.dsabuddies.app.dto.SubmitMockAnswerRequest;
import com.dsabuddies.app.model.*;
import com.dsabuddies.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockSessionRepository sessionRepository;
    private final MockSessionQuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DesignTemplateRepository templateRepository;
    private final LevelService levelService;
    private final ConsistencyService consistencyService;
    private final BadgeService badgeService;

    private static final List<String[]> BEHAVIORAL_BANK = List.of(
        new String[]{"Production Incident Under Pressure", "Describe a time when a critical bug or production incident occurred. How did you diagnose, communicate, and resolve the outage?", "https://www.thebalancecareers.com/star-interview-method-2061629"},
        new String[]{"Technical Architecture Conflict", "Tell me about a situation where you had a strong technical disagreement with a teammate or lead regarding design or technology choice. How did you reach consensus?", "https://www.thebalancecareers.com/star-interview-method-2061629"},
        new String[]{"Tight Deadlines & Scope Negotiation", "Describe a project where requirements changed or deadlines were drastically pulled forward. How did you prioritize engineering trade-offs?", "https://www.thebalancecareers.com/star-interview-method-2061629"},
        new String[]{"Engineering Mentorship & Impact", "Tell me about a time you helped level up another engineer or improved the developer tooling / engineering productivity across your team.", "https://www.thebalancecareers.com/star-interview-method-2061629"}
    );

    @Transactional
    public MockSessionDto startSession(Long userId, StartMockRequest req) {
        User user = userRepository.findById(userId).orElseThrow();

        String mode = (req != null && req.mode() != null) ? req.mode().toUpperCase() : "DSA";
        String difficulty = (req != null && req.difficultyFilter() != null) ? req.difficultyFilter().toUpperCase() : "MIXED";
        int questionCount = (req != null && req.questionCount() != null) ? Math.max(1, Math.min(5, req.questionCount())) : 2;
        int timeLimit = (req != null && req.timeLimitMinutes() != null) ? Math.max(15, Math.min(120, req.timeLimitMinutes())) : 45;

        MockSession session = MockSession.builder()
                .user(user)
                .mode(mode)
                .difficultyFilter(difficulty)
                .topicFilter(req != null ? req.topicFilter() : null)
                .questionCount(questionCount)
                .timeLimitMinutes(timeLimit)
                .startedAt(LocalDateTime.now())
                .status("IN_PROGRESS")
                .build();

        List<MockSessionQuestion> sessionQuestions = new ArrayList<>();

        if ("SYSTEM_DESIGN".equals(mode)) {
            List<DesignTemplate> templates = templateRepository.findAll();
            Collections.shuffle(templates);
            int count = Math.min(questionCount, templates.size());
            for (int i = 0; i < count; i++) {
                DesignTemplate t = templates.get(i);
                sessionQuestions.add(MockSessionQuestion.builder()
                        .session(session)
                        .customTitle(t.getTitle())
                        .customDescription(t.getOverview() + "\n\nRequirements:\n" + t.getRequirements())
                        .difficulty(t.getDifficulty())
                        .topicName(t.getCategory())
                        .questionOrder(i + 1)
                        .build());
            }
        } else if ("BEHAVIORAL".equals(mode)) {
            List<String[]> questions = new ArrayList<>(BEHAVIORAL_BANK);
            Collections.shuffle(questions);
            int count = Math.min(questionCount, questions.size());
            for (int i = 0; i < count; i++) {
                String[] q = questions.get(i);
                sessionQuestions.add(MockSessionQuestion.builder()
                        .session(session)
                        .customTitle(q[0])
                        .customDescription(q[1])
                        .customLink(q[2])
                        .difficulty("MEDIUM")
                        .topicName("Behavioral / STAR")
                        .questionOrder(i + 1)
                        .build());
            }
        } else {
            // DSA Mode: select tasks
            List<Task> allTasks = taskRepository.findAll();
            List<Task> candidates = new ArrayList<>();

            if (!"MIXED".equals(difficulty)) {
                candidates = allTasks.stream()
                        .filter(t -> difficulty.equalsIgnoreCase(t.getDifficulty()))
                        .collect(Collectors.toList());
            }

            if (candidates.size() < questionCount) {
                candidates = new ArrayList<>(allTasks);
            }

            Collections.shuffle(candidates);
            int count = Math.min(questionCount, candidates.size());
            for (int i = 0; i < count; i++) {
                Task t = candidates.get(i);
                sessionQuestions.add(MockSessionQuestion.builder()
                        .session(session)
                        .task(t)
                        .customTitle(t.getTitle())
                        .customDescription(t.getDescription())
                        .customLink(t.getPlatformLink())
                        .difficulty(t.getDifficulty())
                        .topicName(t.getTopic() != null ? t.getTopic().getName() : "Algorithms")
                        .questionOrder(i + 1)
                        .build());
            }
        }

        session.setQuestions(sessionQuestions);
        session = sessionRepository.save(session);
        return toDto(session);
    }

    @Transactional
    public MockSessionDto submitAnswer(Long sessionId, Long questionId, SubmitMockAnswerRequest req, Long userId) {
        MockSession session = sessionRepository.findById(sessionId).orElseThrow();
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        MockSessionQuestion question = questionRepository.findById(questionId).orElseThrow();
        if (!question.getSession().getId().equals(sessionId)) {
            throw new RuntimeException("Question does not belong to session");
        }

        if (req != null) {
            if (req.timeSpentSeconds() != null) question.setTimeSpentSeconds(req.timeSpentSeconds());
            if (req.selfRating() != null) question.setSelfRating(req.selfRating());
            if (req.userNotes() != null) question.setUserNotes(req.userNotes());
            if (req.answered() != null) question.setAnswered(req.answered());
            else question.setAnswered(true);
        } else {
            question.setAnswered(true);
        }

        questionRepository.save(question);
        return toDto(session);
    }

    @Transactional
    public MockSessionDto completeSession(Long sessionId, Long userId) {
        MockSession session = sessionRepository.findById(sessionId).orElseThrow();
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        session.setStatus("COMPLETED");
        session.setCompletedAt(LocalDateTime.now());

        int score = 0;
        int answeredCount = 0;
        for (MockSessionQuestion q : session.getQuestions()) {
            if (q.isAnswered()) {
                answeredCount++;
                if (q.getSelfRating() == null || q.getSelfRating() >= 3) {
                    score++;
                }
            }
        }

        int xpAwarded = 50 + (answeredCount * 30);
        session.setScore(score);
        session.setXpAwarded(xpAwarded);

        User user = session.getUser();
        user.setTotalXp(user.getTotalXp() + xpAwarded);
        levelService.updateLevel(user);
        consistencyService.updateConsistencyScore(user);
        badgeService.checkAndAwardBadges(user, null, null);
        userRepository.save(user);

        session = sessionRepository.save(session);
        return toDto(session);
    }

    @Transactional
    public MockSessionDto abandonSession(Long sessionId, Long userId) {
        MockSession session = sessionRepository.findById(sessionId).orElseThrow();
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        session.setStatus("ABANDONED");
        session.setCompletedAt(LocalDateTime.now());
        session = sessionRepository.save(session);
        return toDto(session);
    }

    public MockSessionDto getSession(Long sessionId, Long userId) {
        MockSession session = sessionRepository.findById(sessionId).orElseThrow();
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return toDto(session);
    }

    public List<MockSessionDto> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    private MockSessionDto toDto(MockSession s) {
        List<MockQuestionDto> questionDtos = s.getQuestions().stream()
                .map(q -> new MockQuestionDto(
                        q.getId(),
                        q.getTask() != null ? q.getTask().getId() : null,
                        q.getCustomTitle(),
                        q.getCustomDescription(),
                        q.getCustomLink(),
                        q.getDifficulty(),
                        q.getTopicName(),
                        q.getQuestionOrder(),
                        q.getTimeSpentSeconds(),
                        q.isAnswered(),
                        q.getSelfRating(),
                        q.getUserNotes()
                )).collect(Collectors.toList());

        return new MockSessionDto(
                s.getId(),
                s.getUser().getId(),
                s.getMode(),
                s.getDifficultyFilter(),
                s.getTopicFilter(),
                s.getQuestionCount(),
                s.getTimeLimitMinutes(),
                s.getStartedAt(),
                s.getCompletedAt(),
                s.getScore(),
                s.getXpAwarded(),
                s.getStatus(),
                questionDtos
        );
    }
}
