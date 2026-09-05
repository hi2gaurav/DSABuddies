package com.dsabuddies.app.service;

import com.dsabuddies.app.model.User;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

@Service
public class LevelService {

    public record LevelInfo(int level, String title, int xpRequired) {}

    private static final TreeMap<Integer, LevelInfo> LEVEL_THRESHOLDS = new TreeMap<>();

    static {
        LEVEL_THRESHOLDS.put(0, new LevelInfo(1, "Novice", 0));
        LEVEL_THRESHOLDS.put(100, new LevelInfo(2, "Apprentice", 100));
        LEVEL_THRESHOLDS.put(250, new LevelInfo(3, "Code Scout", 250));
        LEVEL_THRESHOLDS.put(500, new LevelInfo(4, "Problem Solver", 500));
        LEVEL_THRESHOLDS.put(800, new LevelInfo(5, "Algorithm Knight", 800));
        LEVEL_THRESHOLDS.put(1200, new LevelInfo(6, "Bug Slayer", 1200));
        LEVEL_THRESHOLDS.put(1700, new LevelInfo(7, "Data Structurer", 1700));
        LEVEL_THRESHOLDS.put(2300, new LevelInfo(8, "Pattern Seeker", 2300));
        LEVEL_THRESHOLDS.put(3000, new LevelInfo(9, "Algorithm Ninja", 3000));
        LEVEL_THRESHOLDS.put(4000, new LevelInfo(10, "Code Master", 4000));
        LEVEL_THRESHOLDS.put(5500, new LevelInfo(12, "Senior Architect", 5500));
        LEVEL_THRESHOLDS.put(7500, new LevelInfo(15, "System Architect", 7500));
        LEVEL_THRESHOLDS.put(10000, new LevelInfo(20, "DSA Grandmaster", 10000));
        LEVEL_THRESHOLDS.put(15000, new LevelInfo(25, "Interview Legend", 15000));
        LEVEL_THRESHOLDS.put(25000, new LevelInfo(30, "Coding Deity", 25000));
    }

    public LevelInfo getLevelInfoForXp(int xp) {
        var entry = LEVEL_THRESHOLDS.floorEntry(Math.max(0, xp));
        return entry != null ? entry.getValue() : new LevelInfo(1, "Novice", 0);
    }

    public int getNextLevelXp(int currentXp) {
        var entry = LEVEL_THRESHOLDS.higherEntry(Math.max(0, currentXp));
        return entry != null ? entry.getKey() : currentXp;
    }

    public boolean updateLevel(User user) {
        LevelInfo info = getLevelInfoForXp(user.getTotalXp());
        boolean levelUp = info.level() > user.getLevel();
        user.setLevel(info.level());
        user.setTitle(info.title());
        if (info.level() >= 5 && !user.isStreakFreezeAvailable() && user.getStreakFreezeUsedDate() == null) {
            user.setStreakFreezeAvailable(true);
        }
        return levelUp;
    }
}
