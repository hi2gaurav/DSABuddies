package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.MockSessionDto;
import com.dsabuddies.app.dto.StartMockRequest;
import com.dsabuddies.app.dto.SubmitMockAnswerRequest;
import com.dsabuddies.app.model.*;
import com.dsabuddies.app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockInterviewServiceTest {

    @Mock
    private MockSessionRepository sessionRepository;

    @Mock
    private MockSessionQuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private DesignTemplateRepository templateRepository;

    @Mock
    private LevelService levelService;

    @Mock
    private ConsistencyService consistencyService;

    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private MockInterviewService mockInterviewService;

    private User user;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("tester@example.com").name("Dev").totalXp(100).build();
        task1 = Task.builder().id(10L).title("Two Sum").difficulty("EASY").build();
        task2 = Task.builder().id(20L).title("LRU Cache").difficulty("MEDIUM").build();
    }

    @Test
    void testStartSession_CreatesQuestions() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));
        when(sessionRepository.save(any(MockSession.class))).thenAnswer(i -> {
            MockSession s = i.getArgument(0);
            s.setId(100L);
            return s;
        });

        StartMockRequest req = new StartMockRequest("DSA", "MIXED", null, 2, 45);
        MockSessionDto session = mockInterviewService.startSession(1L, req);

        assertNotNull(session);
        assertEquals(100L, session.id());
        assertEquals("DSA", session.mode());
        assertEquals(2, session.questions().size());
        assertEquals("IN_PROGRESS", session.status());
    }

    @Test
    void testCompleteSession_AwardsXpAndCalculatesScore() {
        MockSession session = MockSession.builder()
                .id(100L)
                .user(user)
                .mode("DSA")
                .status("IN_PROGRESS")
                .questions(new ArrayList<>())
                .build();

        MockSessionQuestion q1 = MockSessionQuestion.builder()
                .id(1L)
                .session(session)
                .task(task1)
                .answered(true)
                .selfRating(4)
                .build();

        session.getQuestions().add(q1);

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(MockSession.class))).thenAnswer(i -> i.getArgument(0));

        MockSessionDto completed = mockInterviewService.completeSession(100L, 1L);

        assertEquals("COMPLETED", completed.status());
        assertEquals(1, completed.score());
        assertEquals(80, completed.xpAwarded()); // 50 base + 30 for 1 answered
        assertEquals(180, user.getTotalXp()); // 100 initial + 80
        verify(levelService).updateLevel(user);
        verify(consistencyService).updateConsistencyScore(user);
    }
}
