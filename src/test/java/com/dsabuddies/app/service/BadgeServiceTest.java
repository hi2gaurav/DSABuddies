package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.BadgeDto;
import com.dsabuddies.app.model.Badge;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskCompletion;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.BadgeRepository;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.UserBadgeRepository;
import com.dsabuddies.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BadgeService badgeService;

    private User user;
    private Badge firstBloodBadge;
    private Badge speedDemonBadge;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").name("Dev").totalXp(50).currentStreak(1).build();

        firstBloodBadge = Badge.builder()
                .id(1L)
                .name("First Blood")
                .description("Solve 1 problem")
                .category("PROBLEMS")
                .criteriaType("PROBLEMS_SOLVED")
                .criteriaValue(1)
                .xpReward(10)
                .rarity("COMMON")
                .build();

        speedDemonBadge = Badge.builder()
                .id(2L)
                .name("Speed Demon")
                .description("Solve in < 10 mins")
                .category("SPECIAL")
                .criteriaType("SPEED_SOLVE")
                .criteriaValue(600)
                .xpReward(50)
                .rarity("RARE")
                .build();
    }

    @Test
    void testCheckAndAwardBadges_AwardsFirstBlood() {
        when(userBadgeRepository.findBadgeIdsByUserId(1L)).thenReturn(List.of());
        when(badgeRepository.findAll()).thenReturn(List.of(firstBloodBadge, speedDemonBadge));
        when(taskCompletionRepository.countByUserId(1L)).thenReturn(1L);

        Task task = Task.builder().id(10L).title("Two Sum").build();
        TaskCompletion completion = TaskCompletion.builder()
                .user(user)
                .task(task)
                .timeSpentSeconds(1200) // 20 mins, not speed demon
                .completedAt(LocalDateTime.of(2026, 9, 6, 14, 0))
                .build();

        List<BadgeDto> awarded = badgeService.checkAndAwardBadges(user, task, completion);

        assertEquals(1, awarded.size());
        assertEquals("First Blood", awarded.get(0).name());
        verify(userBadgeRepository, times(1)).save(any());
        assertEquals(60, user.getTotalXp()); // 50 initial + 10 reward
    }

    @Test
    void testCheckAndAwardBadges_AwardsSpeedDemonWhenEligible() {
        when(userBadgeRepository.findBadgeIdsByUserId(1L)).thenReturn(List.of());
        when(badgeRepository.findAll()).thenReturn(List.of(firstBloodBadge, speedDemonBadge));
        when(taskCompletionRepository.countByUserId(1L)).thenReturn(1L);

        Task task = Task.builder().id(10L).title("Reverse String").build();
        TaskCompletion completion = TaskCompletion.builder()
                .user(user)
                .task(task)
                .timeSpentSeconds(300) // 5 minutes!
                .completedAt(LocalDateTime.of(2026, 9, 6, 15, 0))
                .build();

        List<BadgeDto> awarded = badgeService.checkAndAwardBadges(user, task, completion);

        assertEquals(2, awarded.size()); // Both First Blood and Speed Demon
        assertTrue(awarded.stream().anyMatch(b -> "Speed Demon".equals(b.name())));
    }
}
