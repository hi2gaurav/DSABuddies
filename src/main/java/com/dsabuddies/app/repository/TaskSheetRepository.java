package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.TaskSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskSheetRepository extends JpaRepository<TaskSheet, Long> {
    List<TaskSheet> findByEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate date);
    List<TaskSheet> findAllByOrderByCreatedAtDesc();
}
