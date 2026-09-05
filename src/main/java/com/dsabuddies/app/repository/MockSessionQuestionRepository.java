package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.MockSessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockSessionQuestionRepository extends JpaRepository<MockSessionQuestion, Long> {
    List<MockSessionQuestion> findBySessionIdOrderByQuestionOrderAsc(Long sessionId);

    @Modifying
    @Query("UPDATE MockSessionQuestion m SET m.task = null WHERE m.task.taskSheet.id = :sheetId")
    void setTaskNullByTaskSheetId(@Param("sheetId") Long sheetId);

    @Modifying
    @Query("UPDATE MockSessionQuestion m SET m.task = null WHERE m.task.id = :taskId")
    void setTaskNullByTaskId(@Param("taskId") Long taskId);
}
