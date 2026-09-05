package com.dsabuddies.app.service;

import com.dsabuddies.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelServiceTest {

    private LevelService levelService;

    @BeforeEach
    void setUp() {
        levelService = new LevelService();
    }

    @Test
    void testLevelProgressionAndTitles() {
        assertEquals(1, levelService.getLevelInfoForXp(0).level());
        assertEquals("Novice", levelService.getLevelInfoForXp(0).title());

        assertEquals(2, levelService.getLevelInfoForXp(100).level());
        assertEquals("Apprentice", levelService.getLevelInfoForXp(150).title());

        assertEquals(4, levelService.getLevelInfoForXp(500).level());
        assertEquals("Problem Solver", levelService.getLevelInfoForXp(500).title());

        assertEquals(5, levelService.getLevelInfoForXp(800).level());
        assertEquals("Algorithm Knight", levelService.getLevelInfoForXp(900).title());

        assertEquals(20, levelService.getLevelInfoForXp(10000).level());
        assertEquals("DSA Grandmaster", levelService.getLevelInfoForXp(12000).title());
    }

    @Test
    void testUpdateUserLevelAndStreakFreeze() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .totalXp(0)
                .level(1)
                .title("Novice")
                .streakFreezeAvailable(false)
                .build();

        // No level up yet
        boolean levelUp = levelService.updateLevel(user);
        assertFalse(levelUp);
        assertEquals(1, user.getLevel());

        // Level up to Level 5 (800 XP) -> unlocks streak freeze!
        user.setTotalXp(850);
        levelUp = levelService.updateLevel(user);
        assertTrue(levelUp);
        assertEquals(5, user.getLevel());
        assertEquals("Algorithm Knight", user.getTitle());
        assertTrue(user.isStreakFreezeAvailable());
    }
}
