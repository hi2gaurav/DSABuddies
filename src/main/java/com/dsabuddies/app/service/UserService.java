package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.UserDto;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto getUserById(Long id) {
        return toDto(userRepository.findById(id).orElseThrow());
    }

    public UserDto getUserByEmail(String email) {
        return toDto(userRepository.findByEmailIgnoreCase(email.trim()).orElseThrow());
    }

    public static final String ADMIN_EMAIL = "hi2gauravgb@gmail.com";

    @Transactional
    public User getOrCreateUser(org.springframework.security.oauth2.core.user.OAuth2User principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }
        String email = principal.getAttribute("email");
        if (email == null) {
            email = principal.getName();
        }
        String finalEmail = email.trim().toLowerCase();
        String rawName = principal.getAttribute("name");
        String finalName = (rawName == null || rawName.isBlank()) ? finalEmail.split("@")[0] : rawName;
        String picture = principal.getAttribute("picture");

        String expectedRole = ADMIN_EMAIL.equalsIgnoreCase(finalEmail) ? "ROLE_ADMIN" : "ROLE_USER";

        User user = userRepository.findByEmailIgnoreCase(finalEmail)
                .orElseGet(() -> User.builder()
                        .email(finalEmail)
                        .name(finalName)
                        .avatarUrl(picture)
                        .role(expectedRole)
                        .build());

        // Strictly enforce role: only hi2gauravgb@gmail.com is ADMIN; everyone else is USER
        user.setRole(expectedRole);
        user.setName(finalName);
        if (picture != null) {
            user.setAvatarUrl(picture);
        }
        updateStreak(user);
        return user;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAllByOrderByTotalXpDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        if (user.getLastActiveDate() == null) {
            user.setCurrentStreak(1);
            user.setMaxStreak(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(user.getLastActiveDate(), today);
            if (daysBetween == 1) {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
                if (user.getCurrentStreak() > user.getMaxStreak()) {
                    user.setMaxStreak(user.getCurrentStreak());
                }
            } else if (daysBetween == 2 && user.isStreakFreezeAvailable()) {
                // Streak freeze saves the streak!
                user.setStreakFreezeAvailable(false);
                user.setStreakFreezeUsedDate(today);
                user.setCurrentStreak(user.getCurrentStreak() + 1);
                if (user.getCurrentStreak() > user.getMaxStreak()) {
                    user.setMaxStreak(user.getCurrentStreak());
                }
            } else if (daysBetween > 1) {
                user.setCurrentStreak(1);
            }
        }
        user.setLastActiveDate(today);
        userRepository.save(user);
    }

    @Transactional
    public boolean useStreakFreeze(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (!user.isStreakFreezeAvailable()) {
            return false;
        }
        user.setStreakFreezeAvailable(false);
        user.setStreakFreezeUsedDate(LocalDate.now());
        userRepository.save(user);
        return true;
    }

    @Transactional
    public void setDailyGoal(Long userId, int dailyGoal) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setDailyGoal(Math.max(1, Math.min(20, dailyGoal)));
        userRepository.save(user);
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getCurrentStreak(),
                user.getMaxStreak(),
                user.getTotalXp(),
                user.getCreatedAt(),
                user.getLevel(),
                user.getTitle(),
                user.getDailyGoal(),
                user.getConsistencyScore(),
                user.isStreakFreezeAvailable(),
                user.getStreakFreezeUsedDate()
        );
    }
}
