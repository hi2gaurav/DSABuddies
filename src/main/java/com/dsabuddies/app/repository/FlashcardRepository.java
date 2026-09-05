package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByCategoryIgnoreCase(String category);
    long countByCategoryIgnoreCase(String category);
}
