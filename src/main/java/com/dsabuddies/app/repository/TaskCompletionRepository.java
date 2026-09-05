package com.dsabuddies.app.repository;

import com.dsabuddies.app.dto.LeaderboardEntryDto;
import com.dsabuddies.app.model.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {
    List<TaskCompletion> findByUserId(Long userId);
    Optional<TaskCompletion> findByUserIdAndTaskId(Long userId, Long taskId);
    boolean existsByUserIdAndTaskId(Long userId, Long taskId);
    long countByUserId(Long userId);
    long countByUserIdAndTaskTaskSheetId(Long userId, Long sheetId);
    List<TaskCompletion> findByTaskTaskSheetIdAndUserId(Long sheetId, Long userId);
    void deleteByTaskTaskSheetId(Long sheetId);
    void deleteByTaskId(Long taskId);
    List<TaskCompletion> findByTaskId(Long taskId);
    List<TaskCompletion> findByUserIdAndCompletedAtGreaterThanEqual(Long userId, LocalDateTime since);
    List<TaskCompletion> findByCompletedAtGreaterThanEqual(LocalDateTime since);

    @Query("SELECT new com.dsabuddies.app.dto.LeaderboardEntryDto(0, u.id, u.name, u.avatarUrl, u.totalXp, u.currentStreak, CAST(COUNT(tc) AS int), u.level, u.title) " +
           "FROM User u LEFT JOIN TaskCompletion tc ON tc.user = u " +
           "GROUP BY u.id, u.name, u.avatarUrl, u.totalXp, u.currentStreak, u.level, u.title " +
           "ORDER BY u.totalXp DESC")
    List<LeaderboardEntryDto> findTopUsersByXp();

    @Query("SELECT tc.user.id, tc.user.name, tc.user.avatarUrl, tc.user.currentStreak, tc.user.level, tc.user.title, " +
           "SUM(tc.task.xpReward), COUNT(tc) " +
           "FROM TaskCompletion tc " +
           "WHERE tc.completedAt >= :since " +
           "GROUP BY tc.user.id, tc.user.name, tc.user.avatarUrl, tc.user.currentStreak, tc.user.level, tc.user.title " +
           "ORDER BY SUM(tc.task.xpReward) DESC")
    List<Object[]> findLeaderboardSince(@Param("since") LocalDateTime since);
}
