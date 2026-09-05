package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.FlashcardDto;
import com.dsabuddies.app.model.Flashcard;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.model.UserFlashcardProgress;
import com.dsabuddies.app.repository.FlashcardRepository;
import com.dsabuddies.app.repository.UserFlashcardProgressRepository;
import com.dsabuddies.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private UserFlashcardProgressRepository progressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LevelService levelService;

    @Mock
    private ConsistencyService consistencyService;

    @InjectMocks
    private FlashcardService flashcardService;

    private User user;
    private Flashcard card;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").totalXp(0).build();
        card = Flashcard.builder().id(10L).category("JAVA").question("Q").answer("A").build();
    }

    @Test
    void testSubmitFlashcardReview_AdvancesIntervalAndAwardsXp() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flashcardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(progressRepository.findByUserIdAndFlashcardId(1L, 10L)).thenReturn(Optional.empty());
        when(progressRepository.save(any(UserFlashcardProgress.class))).thenAnswer(i -> i.getArgument(0));

        // Rating 4 (Easy)
        FlashcardDto reviewed = flashcardService.submitFlashcardReview(10L, 1L, 4);

        assertNotNull(reviewed);
        assertEquals(1, reviewed.intervalDays());
        assertEquals(1, reviewed.reviewCount());
        assertEquals(10, user.getTotalXp()); // +10 XP awarded
        verify(levelService).updateLevel(user);
    }
}
