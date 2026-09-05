package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.ReviewItemDto;
import com.dsabuddies.app.model.ReviewQueue;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.ReviewQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private final ReviewQueueRepository reviewQueueRepository;

    @Transactional
    public void scheduleReview(User user, Task task) {
        // If already in queue, reset for fresh review cycle
        ReviewQueue queue = reviewQueueRepository.findByUserIdAndTaskId(user.getId(), task.getId())
                .orElse(ReviewQueue.builder()
                        .user(user)
                        .task(task)
                        .intervalDays(1)
                        .easeFactor(2.5)
                        .reviewCount(0)
                        .build());

        queue.setNextReviewDate(LocalDate.now().plusDays(1));
        queue.setLastReviewedAt(LocalDateTime.now());
        reviewQueueRepository.save(queue);
    }

    @Transactional(readOnly = true)
    public List<ReviewItemDto> getDueReviews(Long userId) {
        return reviewQueueRepository.findDueReviews(userId, LocalDate.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewItemDto> getUpcomingReviews(Long userId) {
        return reviewQueueRepository.findUpcomingReviews(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getDueCount(Long userId) {
        return reviewQueueRepository.countByUserIdAndNextReviewDateLessThanEqual(userId, LocalDate.now());
    }

    /**
     * Implements the SM-2 Spaced Repetition Algorithm:
     * Rating:
     * 1 - Blackout / Failed completely (Interval = 1)
     * 2 - Hard / Incorrect with hesitation (Interval = 1)
     * 3 - Good / Correct with difficulty (Interval advances)
     * 4 - Easy / Correct after hesitation (Interval advances with ease bonus)
     * 5 - Mastered / Perfect recall (Interval advances with max ease)
     */
    @Transactional
    public ReviewItemDto submitReview(Long userId, Long taskId, int rating) {
        ReviewQueue queue = reviewQueueRepository.findByUserIdAndTaskId(userId, taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found in review queue"));

        int interval;
        int reviewCount = queue.getReviewCount();
        double easeFactor = queue.getEaseFactor();

        if (rating < 3) {
            // Failed recall: reset intervals back to 1 day
            interval = 1;
            reviewCount = 0;
        } else {
            // Successful recall: calculate next interval
            if (reviewCount == 0) {
                interval = 1;
            } else if (reviewCount == 1) {
                interval = 6;
            } else {
                interval = (int) Math.round(queue.getIntervalDays() * easeFactor);
            }
            reviewCount++;
        }

        // Update ease factor: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        easeFactor = easeFactor + (0.1 - (5 - rating) * (0.08 + (5 - rating) * 0.02));
        if (easeFactor < 1.3) {
            easeFactor = 1.3; // minimum lower bound for EF
        }

        queue.setIntervalDays(interval);
        queue.setEaseFactor(easeFactor);
        queue.setReviewCount(reviewCount);
        queue.setNextReviewDate(LocalDate.now().plusDays(interval));
        queue.setLastReviewedAt(LocalDateTime.now());

        return toDto(reviewQueueRepository.save(queue));
    }

    private ReviewItemDto toDto(ReviewQueue rq) {
        Task t = rq.getTask();
        return new ReviewItemDto(
                rq.getId(),
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getDifficulty(),
                t.getTopic() != null ? t.getTopic().getName() : null,
                t.getTopic() != null ? t.getTopic().getColor() : null,
                t.getPlatformLink(),
                t.getXpReward(),
                rq.getNextReviewDate(),
                rq.getIntervalDays(),
                rq.getEaseFactor(),
                rq.getReviewCount(),
                rq.getLastReviewedAt()
        );
    }
}
