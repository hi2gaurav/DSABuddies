package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.MockSessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockSessionQuestionRepository extends JpaRepository<MockSessionQuestion, Long> {
    List<MockSessionQuestion> findBySessionIdOrderByQuestionOrderAsc(Long sessionId);
}
