package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.UserFlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFlashcardProgressRepository extends JpaRepository<UserFlashcardProgress, Long> {
    List<UserFlashcardProgress> findByUserId(Long userId);
    Optional<UserFlashcardProgress> findByUserIdAndFlashcardId(Long userId, Long flashcardId);
    List<UserFlashcardProgress> findByUserIdAndNextReviewDateLessThanEqual(Long userId, LocalDate date);
    void deleteByUserId(Long userId);
}
