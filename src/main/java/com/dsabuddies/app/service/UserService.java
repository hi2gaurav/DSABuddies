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
    public static final String ADMIN_EMAIL_ALT = "hi2gauravgb@gmai.com";

    public static boolean isPrimaryAdmin(String email) {
        if (email == null) return false;
        String clean = email.trim().toLowerCase();
        return clean.equals(ADMIN_EMAIL) || clean.equals(ADMIN_EMAIL_ALT);
    }

    public boolean isAdmin(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String email = null;
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauthUser) {
            email = oauthUser.getAttribute("email");
            if (email == null) email = oauthUser.getName();
        } else {
            email = authentication.getName();
        }
        if (isPrimaryAdmin(email)) {
            return true;
        }
        if (email != null && !email.isBlank() && !"anonymousUser".equalsIgnoreCase(email)) {
            return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                    .map(u -> "ROLE_ADMIN".equals(u.getRole()))
                    .orElse(false);
        }
        return false;
    }

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

        String expectedRole = isPrimaryAdmin(finalEmail) ? "ROLE_ADMIN" : "ROLE_USER";

        User user = userRepository.findByEmailIgnoreCase(finalEmail)
                .orElseGet(() -> User.builder()
                        .email(finalEmail)
                        .name(finalName)
                        .avatarUrl(picture)
                        .role(expectedRole)
                        .build());

        // Strictly enforce role: only primary admin is ADMIN; everyone else is USER unless granted
        user.setRole(expectedRole);
        user.setName(finalName);
        if (picture != null) {
            user.setAvatarUrl(picture);
        }
        updateStreak(user);
        return user;
    }

    @Transactional
    public User getAdminUser() {
        return userRepository.findByEmailIgnoreCase(ADMIN_EMAIL).orElseGet(() ->
            userRepository.save(User.builder()
                    .email(ADMIN_EMAIL)
                    .name("Admin")
                    .role("ROLE_ADMIN")
                    .totalXp(0)
                    .currentStreak(1)
                    .maxStreak(1)
                    .build())
        );
    }

    @Transactional
    public User resolveUser(org.springframework.security.core.Authentication authentication, org.springframework.security.oauth2.core.user.OAuth2User oauthUser) {
        if (oauthUser != null) {
            return getOrCreateUser(oauthUser);
        }
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oUser) {
                return getOrCreateUser(oUser);
            }
            String email = authentication.getName();
            if (email != null && !email.isBlank() && !"anonymousUser".equalsIgnoreCase(email)) {
                return userRepository.findByEmailIgnoreCase(email.trim()).orElseGet(() -> {
                    String role = isPrimaryAdmin(email.trim()) ? "ROLE_ADMIN" : "ROLE_USER";
                    return userRepository.save(User.builder()
                            .email(email.trim().toLowerCase())
                            .name(email.split("@")[0])
                            .role(role)
                            .totalXp(0)
                            .currentStreak(1)
                            .maxStreak(1)
                            .build());
                });
            }
        }
        return getAdminUser();
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

    @Transactional
    public UserDto updateMemberStatus(Long userId, String status, Integer muteDurationHours, String reason) {
        User user = userRepository.findById(userId).orElseThrow();
        if (ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Primary admin account status cannot be modified");
        }

        String normalizedStatus = status != null ? status.trim().toUpperCase() : "ACTIVE";
        if (!List.of("ACTIVE", "MUTED", "BANNED").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Must be ACTIVE, MUTED, or BANNED");
        }

        user.setStatus(normalizedStatus);
        user.setModerationReason(reason);

        if ("MUTED".equals(normalizedStatus)) {
            int hours = (muteDurationHours != null && muteDurationHours > 0) ? muteDurationHours : 24;
            user.setMutedUntil(java.time.LocalDateTime.now().plusHours(hours));
        } else if ("ACTIVE".equals(normalizedStatus)) {
            user.setMutedUntil(null);
            user.setModerationReason(null);
        } else if ("BANNED".equals(normalizedStatus)) {
            user.setMutedUntil(null);
        }

        userRepository.save(user);
        return toDto(user);
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
                user.getStreakFreezeUsedDate(),
                user.getStatus() != null ? user.getStatus() : "ACTIVE",
                user.getMutedUntil(),
                user.getModerationReason()
        );
    }
}
