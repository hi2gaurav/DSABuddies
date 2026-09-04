package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTaskSheetId(Long sheetId);
    List<Task> findByTopicId(Long topicId);
    long countByTaskSheetId(Long sheetId);
}
