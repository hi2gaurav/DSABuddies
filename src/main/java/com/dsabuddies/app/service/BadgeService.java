package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.BadgeDto;
import com.dsabuddies.app.model.Badge;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskCompletion;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.model.UserBadge;
import com.dsabuddies.app.repository.BadgeRepository;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.UserBadgeRepository;
import com.dsabuddies.app.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void initDefaultBadges() {
        if (badgeRepository.count() > 0) {
            return;
        }

        List<Badge> defaultBadges = List.of(
            // Streak
            Badge.builder().name("Sparks Flying").description("Maintain a 3-day coding streak").icon("Flame").category("STREAK").criteriaType("STREAK_DAYS").criteriaValue(3).xpReward(25).rarity("COMMON").build(),
            Badge.builder().name("Week Warrior").description("Maintain a 7-day coding streak").icon("Zap").category("STREAK").criteriaType("STREAK_DAYS").criteriaValue(7).xpReward(50).rarity("RARE").build(),
            Badge.builder().name("Fortnight Fighter").description("Maintain a 14-day coding streak").icon("Shield").category("STREAK").criteriaType("STREAK_DAYS").criteriaValue(14).xpReward(100).rarity("RARE").build(),
            Badge.builder().name("Monthly Master").description("Maintain a 30-day coding streak").icon("Award").category("STREAK").criteriaType("STREAK_DAYS").criteriaValue(30).xpReward(250).rarity("EPIC").build(),
            Badge.builder().name("Centurion").description("Maintain a 100-day coding streak").icon("Crown").category("STREAK").criteriaType("STREAK_DAYS").criteriaValue(100).xpReward(1000).rarity("LEGENDARY").build(),

            // Problems
            Badge.builder().name("First Blood").description("Solve your first problem on DSA Buddies").icon("Target").category("PROBLEMS").criteriaType("PROBLEMS_SOLVED").criteriaValue(1).xpReward(10).rarity("COMMON").build(),
            Badge.builder().name("Getting Started").description("Solve 10 problems").icon("CheckCircle2").category("PROBLEMS").criteriaType("PROBLEMS_SOLVED").criteriaValue(10).xpReward(30).rarity("COMMON").build(),
            Badge.builder().name("Quarter Century").description("Solve 25 problems").icon("Sparkles").category("PROBLEMS").criteriaType("PROBLEMS_SOLVED").criteriaValue(25).xpReward(75).rarity("RARE").build(),
            Badge.builder().name("Half Century").description("Solve 50 problems").icon("Medal").category("PROBLEMS").criteriaType("PROBLEMS_SOLVED").criteriaValue(50).xpReward(150).rarity("RARE").build(),
            Badge.builder().name("Century Club").description("Solve 100 problems").icon("Trophy").category("PROBLEMS").criteriaType("PROBLEMS_SOLVED").criteriaValue(100).xpReward(300).rarity("EPIC").build(),
            Badge.builder().name("Grandmaster 250").description("Solve 250 problems").icon("Crown").category("PROBLEMS").criteriaType("PROBLEMS_SOLVED").criteriaValue(250).xpReward(750).rarity("LEGENDARY").build(),

            // XP
            Badge.builder().name("XP Initiate").description("Accumulate 200 total XP").icon("Star").category("XP").criteriaType("XP_EARNED").criteriaValue(200).xpReward(20).rarity("COMMON").build(),
            Badge.builder().name("XP High Roller").description("Accumulate 1,000 total XP").icon("Zap").category("XP").criteriaType("XP_EARNED").criteriaValue(1000).xpReward(50).rarity("RARE").build(),
            Badge.builder().name("XP Titan").description("Accumulate 5,000 total XP").icon("Flame").category("XP").criteriaType("XP_EARNED").criteriaValue(5000).xpReward(200).rarity("EPIC").build(),
            Badge.builder().name("XP Overlord").description("Accumulate 10,000 total XP").icon("Gem").category("XP").criteriaType("XP_EARNED").criteriaValue(10000).xpReward(500).rarity("LEGENDARY").build(),

            // Special
            Badge.builder().name("Speed Demon").description("Solve a problem in under 10 minutes").icon("Timer").category("SPECIAL").criteriaType("SPEED_SOLVE").criteriaValue(600).xpReward(50).rarity("RARE").build(),
            Badge.builder().name("Night Owl").description("Solve a problem between 12 AM and 5 AM").icon("Moon").category("SPECIAL").criteriaType("NIGHT_OWL").criteriaValue(1).xpReward(40).rarity("RARE").build(),
            Badge.builder().name("Flawless Score").description("Complete a Hard problem with a 5-star confidence rating").icon("StarHalf").category("SPECIAL").criteriaType("FLAWLESS_HARD").criteriaValue(5).xpReward(100).rarity("EPIC").build(),
            Badge.builder().name("Consistent Achiever").description("Achieve an 80%+ 30-day consistency score").icon("TrendingUp").category("SPECIAL").criteriaType("CONSISTENCY").criteriaValue(80).xpReward(200).rarity("EPIC").build()
        );

        badgeRepository.saveAll(defaultBadges);
    }

    @Transactional
    public List<BadgeDto> checkAndAwardBadges(User user, Task task, TaskCompletion completion) {
        List<Long> earnedBadgeIds = userBadgeRepository.findBadgeIdsByUserId(user.getId());
        Set<Long> earnedSet = new HashSet<>(earnedBadgeIds);

        List<Badge> allBadges = badgeRepository.findAll();
        List<BadgeDto> newlyEarned = new ArrayList<>();

        long problemsSolved = taskCompletionRepository.countByUserId(user.getId());
        int streak = user.getCurrentStreak();
        int totalXp = user.getTotalXp();
        double consistency = user.getConsistencyScore();

        for (Badge badge : allBadges) {
            if (earnedSet.contains(badge.getId())) {
                continue;
            }

            boolean qualified = false;
            switch (badge.getCriteriaType()) {
                case "STREAK_DAYS" -> qualified = streak >= badge.getCriteriaValue();
                case "PROBLEMS_SOLVED" -> qualified = problemsSolved >= badge.getCriteriaValue();
                case "XP_EARNED" -> qualified = totalXp >= badge.getCriteriaValue();
                case "SPEED_SOLVE" -> {
                    if (completion != null && completion.getTimeSpentSeconds() != null) {
                        qualified = completion.getTimeSpentSeconds() > 0 && completion.getTimeSpentSeconds() <= badge.getCriteriaValue();
                    }
                }
                case "NIGHT_OWL" -> {
                    if (completion != null && completion.getCompletedAt() != null) {
                        int hour = completion.getCompletedAt().getHour();
                        qualified = hour >= 0 && hour <= 5;
                    }
                }
                case "FLAWLESS_HARD" -> {
                    if (task != null && "HARD".equalsIgnoreCase(task.getDifficulty()) && completion != null && completion.getSelfRating() != null) {
                        qualified = completion.getSelfRating() == 5;
                    }
                }
                case "CONSISTENCY" -> qualified = consistency >= badge.getCriteriaValue();
            }

            if (qualified) {
                UserBadge userBadge = UserBadge.builder()
                        .user(user)
                        .badge(badge)
                        .earnedAt(LocalDateTime.now())
                        .build();
                userBadgeRepository.save(userBadge);

                // Add bonus XP
                user.setTotalXp(user.getTotalXp() + badge.getXpReward());
                userRepository.save(user);

                newlyEarned.add(new BadgeDto(
                        badge.getId(),
                        badge.getName(),
                        badge.getDescription(),
                        badge.getIcon(),
                        badge.getCategory(),
                        badge.getCriteriaType(),
                        badge.getCriteriaValue(),
                        badge.getXpReward(),
                        badge.getRarity(),
                        true,
                        userBadge.getEarnedAt(),
                        100.0
                ));
            }
        }

        return newlyEarned;
    }

    public List<BadgeDto> getAllBadgesForUser(Long userId) {
        List<Badge> allBadges = badgeRepository.findAllByOrderByCriteriaValueAsc();
        List<UserBadge> userBadges = userId != null ? userBadgeRepository.findByUserId(userId) : List.of();
        Map<Long, LocalDateTime> earnedMap = userBadges.stream()
                .collect(Collectors.toMap(ub -> ub.getBadge().getId(), UserBadge::getEarnedAt, (a, b) -> a));

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        long problemsSolved = userId != null ? taskCompletionRepository.countByUserId(userId) : 0;
        int streak = user != null ? user.getCurrentStreak() : 0;
        int xp = user != null ? user.getTotalXp() : 0;
        double consistency = user != null ? user.getConsistencyScore() : 0.0;

        return allBadges.stream().map(badge -> {
            boolean earned = earnedMap.containsKey(badge.getId());
            LocalDateTime earnedAt = earnedMap.get(badge.getId());
            double progress = 0.0;

            if (earned) {
                progress = 100.0;
            } else {
                switch (badge.getCriteriaType()) {
                    case "STREAK_DAYS" -> progress = Math.min(100.0, ((double) streak / badge.getCriteriaValue()) * 100.0);
                    case "PROBLEMS_SOLVED" -> progress = Math.min(100.0, ((double) problemsSolved / badge.getCriteriaValue()) * 100.0);
                    case "XP_EARNED" -> progress = Math.min(100.0, ((double) xp / badge.getCriteriaValue()) * 100.0);
                    case "CONSISTENCY" -> progress = Math.min(100.0, (consistency / badge.getCriteriaValue()) * 100.0);
                    default -> progress = 0.0;
                }
                progress = Math.round(progress * 10.0) / 10.0;
            }

            return new BadgeDto(
                    badge.getId(),
                    badge.getName(),
                    badge.getDescription(),
                    badge.getIcon(),
                    badge.getCategory(),
                    badge.getCriteriaType(),
                    badge.getCriteriaValue(),
                    badge.getXpReward(),
                    badge.getRarity(),
                    earned,
                    earnedAt,
                    progress
            );
        }).collect(Collectors.toList());
    }

    public List<BadgeDto> getEarnedBadges(Long userId) {
        return getAllBadgesForUser(userId).stream()
                .filter(BadgeDto::earned)
                .collect(Collectors.toList());
    }
}
