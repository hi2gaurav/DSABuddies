package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserNoteRepository extends JpaRepository<UserNote, Long> {

    @Query("SELECT un FROM UserNote un JOIN FETCH un.task t LEFT JOIN FETCH t.topic WHERE un.user.id = :userId ORDER BY un.updatedAt DESC")
    List<UserNote> findByUserIdWithTask(@Param("userId") Long userId);

    Optional<UserNote> findByUserIdAndTaskId(Long userId, Long taskId);

    void deleteByUserIdAndTaskId(Long userId, Long taskId);

    void deleteByTaskId(Long taskId);

    void deleteByTaskTaskSheetId(Long sheetId);
}
