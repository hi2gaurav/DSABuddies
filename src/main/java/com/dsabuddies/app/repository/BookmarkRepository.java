package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("SELECT b FROM Bookmark b JOIN FETCH b.task t LEFT JOIN FETCH t.topic WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Bookmark> findByUserIdWithTask(@Param("userId") Long userId);

    Optional<Bookmark> findByUserIdAndTaskId(Long userId, Long taskId);

    boolean existsByUserIdAndTaskId(Long userId, Long taskId);

    void deleteByUserIdAndTaskId(Long userId, Long taskId);

    void deleteByTaskId(Long taskId);

    void deleteByTaskTaskSheetId(Long sheetId);
}
