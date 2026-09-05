package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.LeaderboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, Long> {
    List<LeaderboardSnapshot> findByPeriodTypeOrderByCreatedAtDesc(String periodType);
    List<LeaderboardSnapshot> findTop10ByPeriodTypeOrderByCreatedAtDesc(String periodType);
}
