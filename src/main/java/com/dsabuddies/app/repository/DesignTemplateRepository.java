package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.DesignTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignTemplateRepository extends JpaRepository<DesignTemplate, Long> {
    List<DesignTemplate> findByCategoryIgnoreCase(String category);
    List<DesignTemplate> findAllByOrderByCreatedAtDesc();
}
