package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.ReviewItemDto;
import com.dsabuddies.app.model.ReviewQueue;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.ReviewQueueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacedRepetitionServiceTest {

    @Mock
    private ReviewQueueRepository reviewQueueRepository;

    @InjectMocks
    private SpacedRepetitionService spacedRepetitionService;

    private User user;
    private Task task;
    private ReviewQueue queue;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").name("Tester").build();
        Topic topic = Topic.builder().id(1L).name("Arrays").color("#3B82F6").build();
        task = Task.builder().id(10L).title("Two Sum").topic(topic).difficulty("EASY").xpReward(50).build();
        queue = ReviewQueue.builder()
                .id(100L)
                .user(user)
                .task(task)
                .nextReviewDate(LocalDate.now())
                .intervalDays(1)
                .easeFactor(2.5)
                .reviewCount(0)
                .build();
    }

    @Test
    void testSubmitReview_SuccessfulRecall_AdvancesInterval() {
        when(reviewQueueRepository.findByUserIdAndTaskId(1L, 10L)).thenReturn(Optional.of(queue));
        when(reviewQueueRepository.save(any(ReviewQueue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Rating 4 (Easy)
        ReviewItemDto result = spacedRepetitionService.submitReview(1L, 10L, 4);

        assertNotNull(result);
        assertEquals(1, result.intervalDays()); // first successful repetition is 1 day
        assertEquals(1, result.reviewCount());
        assertTrue(result.easeFactor() >= 2.5); // ease factor increases for rating 4
    }

    @Test
    void testSubmitReview_SecondSuccessfulRecall_SetsIntervalTo6() {
        queue.setReviewCount(1);
        queue.setIntervalDays(1);
        when(reviewQueueRepository.findByUserIdAndTaskId(1L, 10L)).thenReturn(Optional.of(queue));
        when(reviewQueueRepository.save(any(ReviewQueue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Rating 5 (Mastered)
        ReviewItemDto result = spacedRepetitionService.submitReview(1L, 10L, 5);

        assertNotNull(result);
        assertEquals(6, result.intervalDays()); // second repetition is 6 days in SM-2
        assertEquals(2, result.reviewCount());
    }

    @Test
    void testSubmitReview_FailedRecall_ResetsIntervalToOne() {
        queue.setReviewCount(3);
        queue.setIntervalDays(15);
        when(reviewQueueRepository.findByUserIdAndTaskId(1L, 10L)).thenReturn(Optional.of(queue));
        when(reviewQueueRepository.save(any(ReviewQueue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Rating 1 (Forgot completely)
        ReviewItemDto result = spacedRepetitionService.submitReview(1L, 10L, 1);

        assertNotNull(result);
        assertEquals(1, result.intervalDays()); // resets to 1
        assertEquals(0, result.reviewCount()); // resets count
        assertTrue(result.easeFactor() < 2.5); // ease factor penalized
    }
}
