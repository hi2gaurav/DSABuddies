package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.ReviewQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewQueueRepository extends JpaRepository<ReviewQueue, Long> {

    Optional<ReviewQueue> findByUserIdAndTaskId(Long userId, Long taskId);

    @Query("SELECT rq FROM ReviewQueue rq JOIN FETCH rq.task t LEFT JOIN FETCH t.topic WHERE rq.user.id = :userId AND rq.nextReviewDate <= :date ORDER BY rq.nextReviewDate ASC")
    List<ReviewQueue> findDueReviews(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT rq FROM ReviewQueue rq JOIN FETCH rq.task t LEFT JOIN FETCH t.topic WHERE rq.user.id = :userId ORDER BY rq.nextReviewDate ASC")
    List<ReviewQueue> findUpcomingReviews(@Param("userId") Long userId);

    long countByUserIdAndNextReviewDateLessThanEqual(Long userId, LocalDate date);

    void deleteByTaskId(Long taskId);

    void deleteByTaskTaskSheetId(Long sheetId);
}
