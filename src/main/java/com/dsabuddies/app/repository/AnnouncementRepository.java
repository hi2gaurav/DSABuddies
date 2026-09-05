package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByActiveTrueOrderByCreatedAtDesc();
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
