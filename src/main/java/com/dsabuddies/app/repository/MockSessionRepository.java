package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.MockSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockSessionRepository extends JpaRepository<MockSession, Long> {
    List<MockSession> findByUserIdOrderByStartedAtDesc(Long userId);
    long countByUserId(Long userId);
}
